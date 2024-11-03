package org.hdmd.hearingdemo.controller;

import lombok.RequiredArgsConstructor;
import org.hdmd.hearingdemo.service.NotificationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
class NotificationController {

    private final NotificationManager notificationManager;

    @PostMapping("/{notificationId}")
    public ResponseEntity<String> markAsRead(@PathVariable String notificationId) {
        notificationManager.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }
}