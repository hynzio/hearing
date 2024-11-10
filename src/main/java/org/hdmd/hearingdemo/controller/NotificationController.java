package org.hdmd.hearingdemo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.service.NotificationManager;
import org.hdmd.hearingdemo.service.NotificationService;
import org.hdmd.hearingdemo.service.NotificationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림 읽음 처리", description =  "위험상황 감지 알림 읽음처리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")

public class NotificationController {

    private final NotificationService notificationService;

    // 알림 읽음 처리
    @PutMapping("/{notificationId}/read")
    @Operation(
            summary = "알림 읽음 처리",
            description = "해당하는 알림 읽음처리")
    public ResponseEntity<String> markAsRead(@PathVariable String notificationId) {
        try {
            // 알림 읽음 처리
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok("알림 읽음 처리 완료");
        } catch (IllegalArgumentException e) {
            // 알림이 없을 경우 404 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(notificationId + "에 해당하는 알림이 존재하지 않습니다.");
        }
    }
}
