package org.hdmd.hearingdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDTO {
    public Long historyId;
    public String timestamp;
    public String location;
    public String filepath;
    public String text;
    public String device;
}

