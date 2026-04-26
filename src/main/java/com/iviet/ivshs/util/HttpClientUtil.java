package com.iviet.ivshs.util;


import com.iviet.ivshs.constant.AppConstant;
import com.fasterxml.jackson.core.type.TypeReference;
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

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

	private static final HttpClient CLIENT = HttpClient.newBuilder()
			.connectTimeout(DEFAULT_TIMEOUT)
			.version(HttpClient.Version.HTTP_2)
			.executor(Executors.newVirtualThreadPerTaskExecutor())
			.build();

	/**
	 * Generic HTTP response wrapper supporting typed deserialization via Jackson.
	 * @param <T> The response body type (use String for raw responses)
	 */
	@Data
	public static class Response<T> {
		private final int statusCode;
		private final String rawBody;
		private final Map<String, String> headers;
		private final Class<T> responseType;
		private final TypeReference<T> typeReference;

		public Response(int statusCode, String rawBody, Map<String, String> headers, Class<T> responseType) {
			this(statusCode, rawBody, headers, responseType, null);
		}

		public Response(int statusCode, String rawBody, Map<String, String> headers, TypeReference<T> typeReference) {
			this(statusCode, rawBody, headers, null, typeReference);
		}

		private Response(int statusCode, String rawBody, Map<String, String> headers, Class<T> responseType, TypeReference<T> typeReference) {
			this.statusCode = statusCode;
			this.rawBody = rawBody;
			this.headers = headers != null ? headers : new HashMap<>();
			this.responseType = responseType;
			this.typeReference = typeReference;
		}

		/**
		 * Deserialize raw response body to typed object using Jackson.
		 * @return Deserialized object, or null if response type is String
		 */
		@SuppressWarnings("unchecked")
		public T getBody() {
			if ((responseType == null || responseType == String.class) && typeReference == null) {
				return (T) rawBody;
			}
			if (rawBody == null || rawBody.isEmpty()) {
				return null;
			}
			if (typeReference != null) {
				return JsonUtil.fromJson(rawBody, typeReference);
			}
			return JsonUtil.fromJson(rawBody, responseType);
		}

		/**
		 * Get raw body as String without deserialization.
		 */
		public String getRawBody() {
			return rawBody;
		}

		/**
		 * Check if response status code indicates success (2xx).
		 */
		public boolean isSuccess() {
			return statusCode >= 200 && statusCode < 300;
		}

		/**
		 * Check HTTP status code and throw appropriate exception for error responses.
		 * <p>Exception mapping:</p>
		 * <ul>
		 * <li>408, 504: NetworkTimeoutException</li>
		 * <li>404: RemoteResourceNotFoundException</li>
		 * <li>5xx: ExternalServiceException</li>
		 * <li>4xx: BadRequestException</li>
		 * </ul>
		 *
		 * @return This response instance for method chaining
		 * @throws NetworkTimeoutException For 408/504
		 * @throws RemoteResourceNotFoundException For 404
		 * @throws ExternalServiceException For 5xx or connection errors
		 * @throws BadRequestException For 4xx (except 404, 408)
		 */
		public Response<T> throwIfError() {
			if (isSuccess()) {
				return this;
			}

			if (statusCode == 408 || statusCode == 504) {
				throw new NetworkTimeoutException("Request timed out");
			}

			if (statusCode == 404) {
				throw new RemoteResourceNotFoundException("Resource not found");
			}

			if (statusCode >= 500) {
				throw new ExternalServiceException("Internal server error: " + rawBody);
			}

			if (statusCode >= 400) {
				throw new BadRequestException("Bad request: " + rawBody);
			}
			return this;
		}
	}

	/**
	 * HTTP request builder supporting fluent API for complex requests.
	 */
	@Data
	@Builder
	public static class Request {
		private String url;
		private String body;
		@Builder.Default
		private Map<String, String> headers = new HashMap<>();
		@Builder.Default
		private Duration timeout = DEFAULT_TIMEOUT;

		/**
		 * Create a request with URL and optional body (for simple cases).
		 * @param url Request URL
		 * @param body Optional request body as String (null allowed)
		 * @return Request instance
		 */
		public static Request of(String url, String body) {
			return builder().url(url).body(body).build();
		}

		/**
		 * Create a request with URL and object body (auto-serialized to JSON).
		 * @param url Request URL
		 * @param bodyObj Object to serialize as JSON
		 * @return Request instance
		 */
		public static Request of(String url, Object bodyObj) {
			String json = bodyObj != null ? JsonUtil.toJson(bodyObj) : null;
			return builder().url(url).body(json).build();
		}

		/**
		 * Create a request with URL, object body, and custom timeout.
		 * @param url Request URL
		 * @param bodyObj Object to serialize as JSON
		 * @param timeout Custom request timeout
		 * @return Request instance
		 */
		public static Request of(String url, Object bodyObj, Duration timeout) {
			String json = bodyObj != null ? JsonUtil.toJson(bodyObj) : null;
			return builder().url(url).body(json).timeout(timeout).build();
		}
	}

	// ===== GET =====
	/**
	 * Send GET request and return raw String response.
	 * @param url Request URL
	 * @return Response<String>
	 */
	public static Response<String> get(String url) {
		return get(url, String.class);
	}

	/**
	 * Send GET request and deserialize response to specified type.
	 * @param url Request URL
	 * @param responseType Target class for deserialization (e.g., DeviceStatusDto.class)
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> get(String url, Class<T> responseType) {
		return get(Request.of(url, (String) null), responseType);
	}

	/**
	 * Send GET request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> get(String url, TypeReference<T> typeReference) {
		return get(Request.of(url, (String) null), typeReference);
	}

	/**
	 * Send GET request using custom Request builder.
	 * @param request Custom request configuration
	 * @return Response<String>
	 */
	public static Response<String> get(Request request) {
		return get(request, String.class);
	}

	/**
	 * Send GET request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> get(Request request, Class<T> responseType) {
		return execute("GET", request, responseType);
	}

	/**
	 * Send GET request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> get(Request request, TypeReference<T> typeReference) {
		return execute("GET", request, typeReference);
	}

	// ===== POST =====
	/**
	 * Send POST request with object body (auto-serialized) and return raw String response.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @return Response<String>
	 */
	public static Response<String> post(String url, Object bodyObj) {
		return post(url, bodyObj, String.class);
	}

	/**
	 * Send POST request with object body and deserialize response to specified type.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param responseType Target class for response deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> post(String url, Object bodyObj, Class<T> responseType) {
		return post(Request.of(url, bodyObj), responseType);
	}

	/**
	 * Send POST request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> post(String url, Object bodyObj, TypeReference<T> typeReference) {
		return post(Request.of(url, bodyObj), typeReference);
	}

	/**
	 * Send POST request using custom Request builder.
	 * @param request Custom request configuration
	 * @return Response<String>
	 */
	public static Response<String> post(Request request) {
		return post(request, String.class);
	}

	/**
	 * Send POST request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> post(Request request, Class<T> responseType) {
		return execute("POST", request, responseType);
	}

	/**
	 * Send POST request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> post(Request request, TypeReference<T> typeReference) {
		return execute("POST", request, typeReference);
	}

	// ===== PUT =====
	/**
	 * Send PUT request with object body (auto-serialized) and return raw String response.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @return Response<String>
	 */
	public static Response<String> put(String url, Object bodyObj) {
		return put(url, bodyObj, String.class);
	}

	/**
	 * Send PUT request with object body and deserialize response to specified type.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param responseType Target class for response deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> put(String url, Object bodyObj, Class<T> responseType) {
		return put(Request.of(url, bodyObj), responseType);
	}

	/**
	 * Send PUT request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> put(String url, Object bodyObj, TypeReference<T> typeReference) {
		return put(Request.of(url, bodyObj), typeReference);
	}

	/**
	 * Send PUT request using custom Request builder.
	 * @param request Custom request configuration
	 * @return Response<String>
	 */
	public static Response<String> put(Request request) {
		return put(request, String.class);
	}

	/**
	 * Send PUT request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> put(Request request, Class<T> responseType) {
		return execute("PUT", request, responseType);
	}

	/**
	 * Send PUT request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> put(Request request, TypeReference<T> typeReference) {
		return execute("PUT", request, typeReference);
	}

	// ===== PATCH =====
	/**
	 * Send PATCH request with object body (auto-serialized) and return raw String response.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @return Response<String>
	 */
	public static Response<String> patch(String url, Object bodyObj) {
		return patch(url, bodyObj, String.class);
	}

	/**
	 * Send PATCH request with object body and deserialize response to specified type.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param responseType Target class for response deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> patch(String url, Object bodyObj, Class<T> responseType) {
		return patch(Request.of(url, bodyObj), responseType);
	}

	/**
	 * Send PATCH request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> patch(String url, Object bodyObj, TypeReference<T> typeReference) {
		return patch(Request.of(url, bodyObj), typeReference);
	}

	/**
	 * Send PATCH request using custom Request builder.
	 * @param request Custom request configuration
	 * @return Response<String>
	 */
	public static Response<String> patch(Request request) {
		return patch(request, String.class);
	}

	/**
	 * Send PATCH request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> patch(Request request, Class<T> responseType) {
		return execute("PATCH", request, responseType);
	}

	/**
	 * Send PATCH request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> patch(Request request, TypeReference<T> typeReference) {
		return execute("PATCH", request, typeReference);
	}

	// ===== DELETE =====
	/**
	 * Send DELETE request and return raw String response.
	 * @param url Request URL
	 * @return Response<String>
	 */
	public static Response<String> delete(String url) {
		return delete(url, String.class);
	}

	/**
	 * Send DELETE request and deserialize response to specified type.
	 * @param url Request URL
	 * @param responseType Target class for deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> delete(String url, Class<T> responseType) {
		return delete(Request.of(url, (String) null), responseType);
	}

	/**
	 * Send DELETE request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> delete(String url, TypeReference<T> typeReference) {
		return delete(Request.of(url, (String) null), typeReference);
	}

	/**
	 * Send DELETE request using custom Request builder.
	 * @param request Custom request configuration
	 * @return Response<String>
	 */
	public static Response<String> delete(Request request) {
		return delete(request, String.class);
	}

	/**
	 * Send DELETE request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> delete(Request request, Class<T> responseType) {
		return execute("DELETE", request, responseType);
	}

	/**
	 * Send DELETE request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return Response<T> with deserialized body
	 */
	public static <T> Response<T> delete(Request request, TypeReference<T> typeReference) {
		return execute("DELETE", request, typeReference);
	}

	// ===== ASYNC GET =====
	/**
	 * Send async GET request and return raw String response.
	 * @param url Request URL
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> getAsync(String url) {
		return getAsync(url, String.class);
	}

	/**
	 * Send async GET request and deserialize response to specified type.
	 * @param url Request URL
	 * @param responseType Target class for deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> getAsync(String url, Class<T> responseType) {
		return getAsync(Request.of(url, (String) null), responseType);
	}

	/**
	 * Send async GET request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> getAsync(String url, TypeReference<T> typeReference) {
		return getAsync(Request.of(url, (String) null), typeReference);
	}

	/**
	 * Send async GET request using custom Request builder.
	 * @param request Custom request configuration
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> getAsync(Request request) {
		return getAsync(request, String.class);
	}

	/**
	 * Send async GET request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> getAsync(Request request, Class<T> responseType) {
		return executeAsync("GET", request, responseType);
	}

	/**
	 * Send async GET request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> getAsync(Request request, TypeReference<T> typeReference) {
		return executeAsync("GET", request, typeReference);
	}

	// ===== ASYNC POST =====
	/**
	 * Send async POST request with object body (auto-serialized) and return raw String response.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> postAsync(String url, Object bodyObj) {
		return postAsync(url, bodyObj, String.class);
	}

	/**
	 * Send async POST request with object body and deserialize response to specified type.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param responseType Target class for response deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> postAsync(String url, Object bodyObj, Class<T> responseType) {
		return postAsync(Request.of(url, bodyObj), responseType);
	}

	/**
	 * Send async POST request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> postAsync(String url, Object bodyObj, TypeReference<T> typeReference) {
		return postAsync(Request.of(url, bodyObj), typeReference);
	}

	/**
	 * Send async POST request using custom Request builder.
	 * @param request Custom request configuration
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> postAsync(Request request) {
		return postAsync(request, String.class);
	}

	/**
	 * Send async POST request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> postAsync(Request request, Class<T> responseType) {
		return executeAsync("POST", request, responseType);
	}

	/**
	 * Send async POST request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> postAsync(Request request, TypeReference<T> typeReference) {
		return executeAsync("POST", request, typeReference);
	}

	// ===== ASYNC PUT =====
	/**
	 * Send async PUT request with object body (auto-serialized) and return raw String response.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> putAsync(String url, Object bodyObj) {
		return putAsync(url, bodyObj, String.class);
	}

	/**
	 * Send async PUT request with object body and deserialize response to specified type.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param responseType Target class for response deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> putAsync(String url, Object bodyObj, Class<T> responseType) {
		return putAsync(Request.of(url, bodyObj), responseType);
	}

	/**
	 * Send async PUT request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> putAsync(String url, Object bodyObj, TypeReference<T> typeReference) {
		return putAsync(Request.of(url, bodyObj), typeReference);
	}

	/**
	 * Send async PUT request using custom Request builder.
	 * @param request Custom request configuration
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> putAsync(Request request) {
		return putAsync(request, String.class);
	}

	/**
	 * Send async PUT request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> putAsync(Request request, Class<T> responseType) {
		return executeAsync("PUT", request, responseType);
	}

	/**
	 * Send async PUT request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> putAsync(Request request, TypeReference<T> typeReference) {
		return executeAsync("PUT", request, typeReference);
	}

	// ===== ASYNC PATCH =====
	/**
	 * Send async PATCH request with object body (auto-serialized) and return raw String response.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> patchAsync(String url, Object bodyObj) {
		return patchAsync(url, bodyObj, String.class);
	}

	/**
	 * Send async PATCH request with object body and deserialize response to specified type.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param responseType Target class for response deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> patchAsync(String url, Object bodyObj, Class<T> responseType) {
		return patchAsync(Request.of(url, bodyObj), responseType);
	}

	/**
	 * Send async PATCH request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param bodyObj Object to serialize as JSON body
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> patchAsync(String url, Object bodyObj, TypeReference<T> typeReference) {
		return patchAsync(Request.of(url, bodyObj), typeReference);
	}

	/**
	 * Send async PATCH request using custom Request builder.
	 * @param request Custom request configuration
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> patchAsync(Request request) {
		return patchAsync(request, String.class);
	}

	/**
	 * Send async PATCH request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> patchAsync(Request request, Class<T> responseType) {
		return executeAsync("PATCH", request, responseType);
	}

	/**
	 * Send async PATCH request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> patchAsync(Request request, TypeReference<T> typeReference) {
		return executeAsync("PATCH", request, typeReference);
	}

	// ===== ASYNC DELETE =====
	/**
	 * Send async DELETE request and return raw String response.
	 * @param url Request URL
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> deleteAsync(String url) {
		return deleteAsync(url, String.class);
	}

	/**
	 * Send async DELETE request and deserialize response to specified type.
	 * @param url Request URL
	 * @param responseType Target class for deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> deleteAsync(String url, Class<T> responseType) {
		return deleteAsync(Request.of(url, (String) null), responseType);
	}

	/**
	 * Send async DELETE request and deserialize response using TypeReference.
	 * @param url Request URL
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> deleteAsync(String url, TypeReference<T> typeReference) {
		return deleteAsync(Request.of(url, (String) null), typeReference);
	}

	/**
	 * Send async DELETE request using custom Request builder.
	 * @param request Custom request configuration
	 * @return CompletableFuture<Response<String>>
	 */
	public static CompletableFuture<Response<String>> deleteAsync(Request request) {
		return deleteAsync(request, String.class);
	}

	/**
	 * Send async DELETE request using custom Request builder with type deserialization.
	 * @param request Custom request configuration
	 * @param responseType Target class for deserialization
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> deleteAsync(Request request, Class<T> responseType) {
		return executeAsync("DELETE", request, responseType);
	}

	/**
	 * Send async DELETE request using custom Request builder with TypeReference.
	 * @param request Custom request configuration
	 * @param typeReference Jackson TypeReference for generic types
	 * @return CompletableFuture<Response<T>>
	 */
	public static <T> CompletableFuture<Response<T>> deleteAsync(Request request, TypeReference<T> typeReference) {
		return executeAsync("DELETE", request, typeReference);
	}

	// ===== Utility Methods =====


	/**
	 * Deprecated: Use handleStatusCode(int, String) instead.
	 * Kept for backward compatibility.
	 */
	@Deprecated(since = "2.0", forRemoval = false)
	public static void handleThrowException(Response<?> response) {
		response.throwIfError();
	}

	// ===== Core Execution =====
	/**
	 * Execute synchronous HTTP request with generic response type support.
	 */
	private static <T> Response<T> execute(String method, Request request, Class<T> responseType) {
		return execute(method, request, responseType, null);
	}

	private static <T> Response<T> execute(String method, Request request, TypeReference<T> typeReference) {
		return execute(method, request, null, typeReference);
	}

	private static <T> Response<T> execute(String method, Request request, Class<T> responseType, TypeReference<T> typeReference) {
		try {
			HttpRequest httpRequest = buildRequest(method, request);
			HttpResponse<String> response = CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			log.info("Executing HTTP {} on thread: name={}, isVirtual={}", method, Thread.currentThread().getName(), Thread.currentThread().isVirtual());
			return mapResponse(response, responseType, typeReference);
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

	/**
	 * Execute asynchronous HTTP request with generic response type support.
	 */
	private static <T> CompletableFuture<Response<T>> executeAsync(String method, Request request, Class<T> responseType) {
		return executeAsync(method, request, responseType, null);
	}

	private static <T> CompletableFuture<Response<T>> executeAsync(String method, Request request, TypeReference<T> typeReference) {
		return executeAsync(method, request, null, typeReference);
	}

	private static <T> CompletableFuture<Response<T>> executeAsync(String method, Request request, Class<T> responseType, TypeReference<T> typeReference) {
		HttpRequest httpRequest = buildRequest(method, request);
		return CLIENT.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
			.thenApply(response -> mapResponse(response, responseType, typeReference))
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
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.header("User-Agent", AppConstant.APP_USER_AGENT);

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

	private static <T> Response<T> mapResponse(HttpResponse<String> response, Class<T> responseType, TypeReference<T> typeReference) {
		Map<String, String> responseHeaders = new HashMap<>();
		response.headers().map().forEach((key, values) -> {
			if (values != null && !values.isEmpty()) {
				responseHeaders.put(key, values.get(0));
			}
		});

		if (typeReference != null) {
			return new Response<>(response.statusCode(), response.body(), responseHeaders, typeReference);
		}
		return new Response<>(response.statusCode(), response.body(), responseHeaders, responseType);
	}
}
