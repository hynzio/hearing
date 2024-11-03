package org.hdmd.hearingdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceManageDTO {
    private Long deviceId;
    private String address;
    private String deviceName;
    private String deviceNum;
}
