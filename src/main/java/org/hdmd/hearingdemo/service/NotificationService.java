package org.hdmd.hearingdemo.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    @Value("${fcm.token}")
    private String token;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    // 위험 알림 전송 메서드
    public void sendDangerNotification(Long recordingId) {
        String notificationId = NotificationManager.createNotification();

        // FCM 메시지 빌더
        Message message = createMessageForDangerNotification(recordingId, notificationId);

        // 메시지 전송
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent danger notification: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 5분마다 읽음 상태 확인 및 재전송
        executor.scheduleAtFixedRate(() -> {
            try {
                NotificationStatus status = NotificationManager.getStatus(notificationId);
                if (status != null && !status.isRead() && status.getResendCount() < 3) {
                    resendDangerNotification(message, status);
                }
            } catch (ResponseStatusException e) {
                // 알림이 없을 경우 예외 처리
                System.out.println("Notification not found, stopping resend attempts.");
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private Message createMessageForDangerNotification(Long recordingId, String notificationId) {
        // 현재 시간 생성 및 포맷
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedTimestamp = now.format(formatter);

        return Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle("위험 감지")
                        .setBody("위험이 감지되었습니다.")
                        .build())
                .putData("recordingId", String.valueOf(recordingId))
                .putData("notificationId", notificationId)
                .putData("timestamp", formattedTimestamp)
                .build();
    }

    private void resendDangerNotification(Message message, NotificationStatus status) {
        try {
            String resendResponse = FirebaseMessaging.getInstance().send(message);
            System.out.println("Resending danger notification: " + resendResponse);
            status.incrementResendCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 외출/귀가 알림 전송 메서드
    public void sendExitOrReturnNotification(Boolean newStatus) {
        String alertTitle = newStatus ? "외출 알림" : "귀가 알림";
        String alertBody = newStatus ? "외출이 감지되었습니다." : "귀가가 감지되었습니다.";

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(alertTitle)
                        .setBody(alertBody)
                        .build())
                .build();

        // 메시지 전송
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent exit/return notification: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markAsRead(String notificationId) {
        NotificationStatus status = NotificationManager.getStatus(notificationId);
        if (status == null) {
            throw new IllegalArgumentException("Notification with id " + notificationId + " not found.");
        }
        status.setRead(true);
    }
}