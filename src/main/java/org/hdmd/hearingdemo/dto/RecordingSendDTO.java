package org.hdmd.hearingdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecordingSendDTO {
    public Long RecordingId;
    public String location;
    public boolean isDangerous;
}