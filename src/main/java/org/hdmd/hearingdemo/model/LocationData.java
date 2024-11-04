package org.hdmd.hearingdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationData {
    private double latitude;
    private double longitude;
    private String timestamp;
}
