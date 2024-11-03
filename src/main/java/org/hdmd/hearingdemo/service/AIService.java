package org.hdmd.hearingdemo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final RestTemplate restTemplate;

    @Value("${ai.api.url}")
    private String aiApiUrl;

    public AIService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public List<String> analyzeTextForDanger(Map<String, Object> sttResult) throws JsonProcessingException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // JSON 형식 설정

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(sttResult);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(aiApiUrl, request, String.class);

            // 위험 문장 리스트 생성
            List<String> dangerousSentences = new ArrayList<>();

            if (response.getStatusCode().is2xxSuccessful()) {
                // JSON 응답 파싱
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode sentencesNode = rootNode.path("sentences");

                // 문장별 prediction이 danger인 것만 리스트에 추가
                for (JsonNode sentenceNode : sentencesNode) {
                    String prediction = sentenceNode.path("prediction").asText();
                    String dangerousText = sentenceNode.path("text").asText();

                    if ("danger".equals(prediction) && !dangerousSentences.contains(dangerousText)) {
                        dangerousSentences.add(dangerousText);  // 위험 문장 저장


                    }
                }
            }
            return dangerousSentences;
        } catch (Exception e) {
            throw new RuntimeException("AI 호출 중 서버 오류가 발생했습니다.");
        }
    }
}
