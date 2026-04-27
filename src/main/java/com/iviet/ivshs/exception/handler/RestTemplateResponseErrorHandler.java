package com.iviet.ivshs.exception.handler;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.DefaultResponseErrorHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestTemplateResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
        // Trả về false để RestTemplate KHÔNG tự động ném exception khi gặp 4xx/5xx.
        // Điều này cho phép caller nhận được ResponseEntity và tự xử lý lỗi (vd: gom nhóm lỗi).
        return false;
    }

    @Override
    public void handleError(@NonNull ClientHttpResponse response) throws IOException {
        // Không làm gì vì hasError đã trả về false
    }
}
