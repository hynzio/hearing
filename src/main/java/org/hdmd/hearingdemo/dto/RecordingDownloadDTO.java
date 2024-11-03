package org.hdmd.hearingdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class RecordingDownloadDTO {
    private Long recordingId;
    private String filepath;
    private List<String> text;
    private String status;
}
