package org.hdmd.hearingdemo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(EntityListeners.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "녹음파일 엔티티")
@Table(name = "recording")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recording_id", nullable = false)
    @Schema(description = "녹음파일 아이디")
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "recording_status")
    @Schema(description = "녹음파일 상태")
    private String status;

    @Column(name = "created_at")
    @Schema(description = "녹음파일 생성시점")
    private String timestamp;

    @Column(name = "longitude")
    @Schema(description = "단말기 위치: 경도")
    private double longitude;

    @Column(name = "latitude")
    @Schema(description = "단말기 위치: 위도")
    private double latitude;

    @Column
    @Schema(description = "파일경로")
    private String filepath;

    @Column
    @Schema(description = "위험 문장")
    private String text;

    @Column
    @Schema(description = "AI 판단 위험여부")
    private boolean aiReview = false;

    @Column
    @Schema(description = "사용자 판단 위험여부")
    private boolean userReview = false;

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
    public List<String> getTextAsList() {
        if (this.text == null || this.text.isEmpty()) {
            return new ArrayList<>();  // 빈 문자열이거나 null이면 빈 리스트 반환
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(this.text, List.class);  // JSON 배열을 List<String>으로 변환
        } catch (Exception e) {
            return new ArrayList<>();  // 예외가 발생하면 빈 리스트 반환
        }
    }

    // JSON 응답시 text를 String으로 반환
    @JsonGetter("text")
    public String getTextAsString() {
        List<String> sentences = getTextAsList();
        if (sentences.isEmpty()) {
            return "";  // 빈 리스트일 경우 빈 문자열 반환
        }
        return String.join(", ", sentences);  // 문장을 쉼표로 연결하여 반환
    }
}