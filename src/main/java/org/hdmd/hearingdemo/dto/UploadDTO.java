package org.hdmd.hearingdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class UploadDTO {
    private Long deviceId;
    private String filepath;
    private String timestamp;
    private Double latitude;
    private Double longitude;
}