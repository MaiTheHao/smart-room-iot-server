package com.iviet.ivshs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.NetworkTimeoutException;
import com.iviet.ivshs.exception.domain.RemoteResourceNotFoundException;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Slf4j
public class HttpClientUtil {

	// Hiện tại 15s là timeout mặc định, do phía thiết bị IoT có độ trễ cao, chưa có giải pháp tối ưu hơn.
	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String ACCEPT_HEADER = "Accept";
	private static final String APPLICATION_JSON = "application/json";

	private static final HttpClient CLIENT = HttpClient.newBuilder()
			.connectTimeout(DEFAULT_TIMEOUT)
			.version(HttpClient.Version.HTTP_2)
			.executor(Executors.newVirtualThreadPerTaskExecutor())
			.build();

	private static final ObjectMapper MAPPER = new ObjectMapper();

	static {
		MAPPER.registerModule(new JavaTimeModule());
		MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Data
	@Builder
	public static class Response {
		private int statusCode;
		private String body;
		private Map<String, String> headers;
		public boolean isSuccess() {
			return statusCode >= 200 && statusCode < 300;
		}
	}

	@Data
	@Builder
	public static class Request {
		private String url;
		private String body;
		@Builder.Default
		private Map<String, String> headers = new HashMap<>();
		@Builder.Default
		private Duration timeout = DEFAULT_TIMEOUT;
	}

	// ===== JSON Utilities =====
	public static String toJson(Object obj) {
		if (obj == null) return null;
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize object to JSON: {}", e.getMessage(), e);
			throw new IllegalArgumentException("JSON serialization error", e);
		}
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		if (json == null || json.isEmpty()) return null;
		
		try {
			return MAPPER.readValue(json, clazz);
		} catch (Exception e) {
			log.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
			throw new IllegalArgumentException("JSON deserialization error", e);
		}
	}

	// ===== GET =====
	public static Response get(String url) {
		return get(Request.builder().url(url).build());
	}

	public static Response get(Request request) {
		return execute("GET", request);
	}

	// ===== POST =====
	public static Response post(String url, String body) {
		return post(Request.builder().url(url).body(body).build());
	}

	public static Response post(String url, Object bodyObj) {
		String json = toJson(bodyObj);
		return post(Request.builder().url(url).body(json).build());
	}

	public static Response post(Request request) {
		return execute("POST", request);
	}

	// ===== PUT =====
	public static Response put(String url, String body) {
		return put(Request.builder().url(url).body(body).build());
	}

	public static Response put(String url, Object bodyObj) {
		String json = toJson(bodyObj);
		return put(Request.builder().url(url).body(json).build());
	}

	public static Response put(Request request) {
		return execute("PUT", request);
	}

	// ===== PATCH =====
	public static Response patch(String url, String body) {
		return patch(Request.builder().url(url).body(body).build());
	}

	public static Response patch(String url, Object bodyObj) {
		String json = toJson(bodyObj);
		return patch(Request.builder().url(url).body(json).build());
	}

	public static Response patch(Request request) {
		return execute("PATCH", request);
	}

	// ===== DELETE =====
	public static Response delete(String url) {
		return delete(Request.builder().url(url).build());
	}

	public static Response delete(Request request) {
		return execute("DELETE", request);
	}

	// ===== ASYNC GET =====
	public static CompletableFuture<Response> getAsync(String url) {
		return getAsync(Request.builder().url(url).build());
	}

	public static CompletableFuture<Response> getAsync(Request request) {
		return executeAsync("GET", request);
	}

	// ===== ASYNC POST =====
	public static CompletableFuture<Response> postAsync(String url, String body) {
		return postAsync(Request.builder().url(url).body(body).build());
	}

	public static CompletableFuture<Response> postAsync(String url, Object bodyObj) {
		String json = toJson(bodyObj);
		return postAsync(Request.builder().url(url).body(json).build());
	}

	public static CompletableFuture<Response> postAsync(Request request) {
		return executeAsync("POST", request);
	}

	// ===== ASYNC PUT =====
	public static CompletableFuture<Response> putAsync(String url, String body) {
		return putAsync(Request.builder().url(url).body(body).build());
	}

	public static CompletableFuture<Response> putAsync(String url, Object bodyObj) {
		String json = toJson(bodyObj);
		return putAsync(Request.builder().url(url).body(json).build());
	}

	public static CompletableFuture<Response> putAsync(Request request) {
		return executeAsync("PUT", request);
	}

	// ===== ASYNC PATCH =====
	public static CompletableFuture<Response> patchAsync(String url, String body) {
		return patchAsync(Request.builder().url(url).body(body).build());
	}

	public static CompletableFuture<Response> patchAsync(String url, Object bodyObj) {
		String json = toJson(bodyObj);
		return patchAsync(Request.builder().url(url).body(json).build());
	}

	public static CompletableFuture<Response> patchAsync(Request request) {
		return executeAsync("PATCH", request);
	}

	// ===== ASYNC DELETE =====
	public static CompletableFuture<Response> deleteAsync(String url) {
		return deleteAsync(Request.builder().url(url).build());
	}

	public static CompletableFuture<Response> deleteAsync(Request request) {
		return executeAsync("DELETE", request);
	}

	// ===== Utility Methods =====

	/**
     * Chủ động kiểm tra và ném (throw) các ngoại lệ (exception) tương ứng dựa trên 
     * mã trạng thái (status code) từ phản hồi của dịch vụ thứ ba.
     * * <p>Logic xử lý cụ thể:</p>
     * <ul>
     * <li><b>2xx:</b> Không làm gì nếu phản hồi thành công.</li>
     * <li><b>408, 504:</b> Ném {@link NetworkTimeoutException} (Lỗi quá hạn kết nối).</li>
     * <li><b>404:</b> Ném {@link RemoteResourceNotFoundException} (Không tìm thấy tài nguyên).</li>
     * <li><b>5xx:</b> Ném {@link ExternalServiceException} cho các lỗi hệ thống từ phía server đối tác.</li>
     * <li><b>4xx:</b> Ném {@link BadRequestException} cho các lỗi yêu cầu không hợp lệ từ phía client.</li>
     * </ul>
     *
     * @param response Đối tượng phản hồi nhận được từ yêu cầu API.
     * @throws NetworkTimeoutException Nếu status code là 408 hoặc 504.
     * @throws RemoteResourceNotFoundException Nếu status code là 404.
     * @throws ExternalServiceException Nếu status code >= 500 (ngoại trừ 504).
     * @throws BadRequestException Nếu status code >= 400 (ngoại trừ 404, 408).
     */
    public static void handleThrowException(Response response) {
        if (response.isSuccess()) return;

        int status = response.getStatusCode();

        if (status == 408 || status == 504) {
            throw new NetworkTimeoutException("Request timed out");
        }

        if (status == 404) {
            throw new RemoteResourceNotFoundException("Resource not found");
        }

        if (status >= 500) {
            throw new ExternalServiceException("Internal server error: " + response.getBody());
        }

        if (status >= 400) {
            throw new BadRequestException("Bad request: " + response.getBody());
        }
    }

	// ===== Core Execution =====
	private static Response execute(String method, Request request) {
		try {
			HttpRequest httpRequest = buildRequest(method, request);
			HttpResponse<String> response = CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			log.info("Executing HTTP {} on thread: name={}, isVirtual={}", method, Thread.currentThread().getName(), Thread.currentThread().isVirtual());
			return mapResponse(response, method, request.getUrl());
		} catch (java.net.http.HttpTimeoutException e) {
			log.error("HTTP {} request to {} timed out: {}", method, request.getUrl(), e.getMessage());
			throw new NetworkTimeoutException("Connection timed out: " + e.getMessage());
		} catch (java.net.ConnectException e) {
			log.error("HTTP {} request to {} failed to connect: {}", method, request.getUrl(), e.getMessage());
			throw new ExternalServiceException("Failed to connect to device: " + e.getMessage());
		} catch (Exception e) {
			log.error("HTTP {} request to {} failed: {}", method, request.getUrl(), e.getMessage(), e);
			throw new ExternalServiceException("Unexpected error during HTTP request: " + e.getMessage());
		}
	}

	private static CompletableFuture<Response> executeAsync(String method, Request request) {
		HttpRequest httpRequest = buildRequest(method, request);
		return CLIENT.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
				.thenApply(response -> mapResponse(response, method, request.getUrl()))
				.exceptionally(ex -> {
					Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
					log.error("Async HTTP {} request to {} failed: {}", method, request.getUrl(), cause.getMessage(), cause);
					
					if (cause instanceof java.net.http.HttpConnectTimeoutException || cause instanceof java.net.http.HttpTimeoutException) {
						throw new NetworkTimeoutException("Connection timed out: " + cause.getMessage());
					} else if (cause instanceof java.net.ConnectException) {
						throw new ExternalServiceException("Failed to connect to device: " + cause.getMessage());
					}
					throw new ExternalServiceException("Unexpected error during async HTTP request: " + cause.getMessage());
				});
	}

	private static HttpRequest buildRequest(String method, Request request) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(request.getUrl()))
				.timeout(request.getTimeout())
				.header(CONTENT_TYPE_HEADER, APPLICATION_JSON)
				.header(ACCEPT_HEADER, APPLICATION_JSON);

		if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
			request.getHeaders().forEach(builder::header);
		}

		HttpRequest.BodyPublisher bodyPublisher = request.getBody() != null && !request.getBody().isEmpty()
				? HttpRequest.BodyPublishers.ofString(request.getBody())
				: HttpRequest.BodyPublishers.noBody();

		return switch (method) {
			case "GET" -> builder.GET().build();
			case "POST" -> builder.POST(bodyPublisher).build();
			case "PUT" -> builder.PUT(bodyPublisher).build();
			case "PATCH" -> builder.method("PATCH", bodyPublisher).build();
			case "DELETE" -> builder.DELETE().build();
			default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
		};
	}

	private static Response mapResponse(HttpResponse<String> response, String method, String url) {
		Map<String, String> responseHeaders = new HashMap<>();
		response.headers().map().forEach((key, values) -> {
			if (values != null && !values.isEmpty()) {
				responseHeaders.put(key, values.get(0));
			}
		});

		log.info("HTTP {} {} returned status code: {}", method, url, response.statusCode());

		return Response.builder()
				.statusCode(response.statusCode())
				.body(response.body())
				.headers(responseHeaders)
				.build();
	}
}
