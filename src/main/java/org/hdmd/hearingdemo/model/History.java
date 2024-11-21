package org.hdmd.hearingdemo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity @Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Schema(description = "기록 엔티티")
@Table(name="history")
public class History {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="history_id", nullable = false)
    @Schema(description = "기록 아이디")
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id",nullable = false)
    @Schema(description = "소속 단말기")
    private Device device;

    @Column(name = "created_at")
    @Schema(description = "생성 시각")
    private String timestamp;

    @Column(name = "location")
    @Schema(description = "발화 위치 주소 정보")
    private String location;

    @Column(name = "filepath")
    @Schema(description = "파일 저장 경로")
    private String filepath;

    @Column(name= "text")
    @Schema(description = "발화 텍스트")
    private String text;

    // List<String>을 text 필드에 설정
    public void setText(List<String> sentences) throws Exception {
        if (sentences == null || sentences.isEmpty()) {
            this.text = "[]";  // 빈 리스트를 빈 JSON 배열로 설정
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            this.text = objectMapper.writeValueAsString(sentences);  // List를 JSON 배열로 변환
        }
    }

    // 텍스트를 List<String>으로 변환
    public String getTextAsList() {
        if (this.text == null || this.text.isEmpty()) {
            return String.valueOf(new ArrayList<>());  // 빈 문자열이거나 null이면 빈sp 리스트 반환
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(this.text, List.class).toString();  // JSON 배열을 List<String>으로 변환
        } catch (Exception e) {
            return String.valueOf(new ArrayList<>());  // 예외가 발생하면 빈 리스트 반환
        }
    }

    // JSON 응답시 text를 String으로 반환
    @JsonGetter("text")
    public String getTextAsString() {
        List<String> sentences = Collections.singletonList(getTextAsList());
        if (sentences.isEmpty()) {
            return "";  // 빈 리스트일 경우 빈 문자열 반환
        }
        return String.join(", ", sentences);  // 문장을 쉼표로 연결하여 반환
    }
}
