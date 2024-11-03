package org.hdmd.hearingdemo.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class UserDTO {
    private Long id;
    private String fcmToken;
    private LocalDateTime lastUpdated;

}
