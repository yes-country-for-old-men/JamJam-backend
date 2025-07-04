package com.jamjam.payment.util;

import com.jamjam.payment.dto.CancellationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortOneApiClient {
    private final WebClient portOneWebClient;

    @Value("${port-one.api.secret-key}")
    private String apiToken;

    /*단건 조회 API 호출
    * paymentId 결제 ID*/
    public Map<String, Object> getPaymentDetails(String paymentId) {
        return portOneWebClient
                .get()
                .uri("/{paymentId}", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
    /*결제 취소 API 호출*/
    public CancellationResponse cancelPayment(String paymentId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "결제 정보 일치하지 않음");

        return portOneWebClient
                .post()
                .uri("/{paymentId}/cancel", paymentId)
                .header("Authorization", "PortOne " + apiToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(CancellationResponse.class)
                .block();
    }
}
