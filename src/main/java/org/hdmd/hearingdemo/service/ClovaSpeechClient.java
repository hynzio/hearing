package org.hdmd.hearingdemo.service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
public class ClovaSpeechClient {
    private static final Logger logger = LoggerFactory.getLogger(ClovaSpeechClient.class); // Logger 추가
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${clova.apiKey}")
    private String apiKey;
    @Value("${clova.invokeUrl}")
    private String invoke;

    // 클로바 STT API 호출 메서드
    public Map<String, Object> soundToText(File audioFile) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-CLOVASPEECH-API-KEY", apiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("media", new FileSystemResource(audioFile));
        String paramsJson = "{\"language\":\"ko-KR\",\"completion\":\"sync\",\"callback\":\"\",\"fullText\":true}";
        body.add("params", paramsJson); // JSON 형식으로 params 추가
        body.add("type", "application/json"); // type 추가

            // 요청 구성
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    invoke + "/recognizer/upload",
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );
            if (responseEntity.getStatusCode().is2xxSuccessful()) {

                JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
                String fullSentences = jsonNode.get("text").asText();
                logger.debug("Full Sentences: {}", fullSentences);
                List<String> sentences = splitSentences(fullSentences);
                result.put("sentences", sentences);

            } else {
                result.put("error", "API 호출 실패: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "예외 발생: " + e.getMessage());
        }

            // API 호출 및 응답 처리
        return result;
    }



    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();

        // 문장 끝 구분자를 기준으로 나누기
        String[] splitText = text.split("(?<=[.!?])\\s*"); // 문장 끝 구분자 (점, 느낌표, 물음표)
        for (String sentence : splitText) {
            sentence = sentence.trim(); // 양쪽 공백 제거
            if (!sentence.isEmpty()) {
                sentences.add(sentence); // 비어 있지 않은 문장만 추가
            }
        }
        return sentences;
    }
}