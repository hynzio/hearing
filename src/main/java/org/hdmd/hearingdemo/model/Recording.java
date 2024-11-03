package org.hdmd.hearingdemo.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

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

    @Column @Schema(description = "사용자 판단 위험여부")
    private boolean userReview = false;

    public List getTextAsList() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(text, List.class);
    }

    public void setText(List<String> sentenses) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.text = objectMapper.writeValueAsString(sentenses);
    }
}