package com.eyes.eyesTools.service.httpUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * @author eyes
 * @date 2022/12/29 16:10
 */
@Slf4j
public class CustomClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequestDetails(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        logResponseDetails(response);
        return execution.execute(request, body);
    }

    // 打印请求明细
    private void logRequestDetails(HttpRequest request, byte[] body) {
        log.debug("-----------------------Start requesting third-party apis----------------------------");
        log.debug("--------------------------------Request details-------------------------------------");
        log.debug("---Headers: {}", request.getHeaders());
        log.debug("---body: {}", new String(body, StandardCharsets.UTF_8));
        log.debug("---{}：{}", request.getMethod(), request.getURI());
    }

    // 打印响应明细
    private void logResponseDetails(ClientHttpResponse response) throws IOException {
        log.debug("--------------------------------Response details------------------------------------");
        log.debug("---Status code  : {}", response.getStatusCode());
        log.debug("---Status text  : {}", response.getStatusText());
        log.debug("---Headers      : {}", response.getHeaders());
        log.debug("---Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
        log.debug("-----------------------End of third-party API response------------------------------");
    }
}