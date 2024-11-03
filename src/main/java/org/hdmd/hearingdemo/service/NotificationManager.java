package org.hdmd.hearingdemo.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationManager {

    // 알림 상태를 관리하는 메모리 내 저장소
    private static final ConcurrentHashMap<String, NotificationStatus> notifications = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 알림 ID 생성 및 알림 등록
    public static String createNotification() {
        String notificationId = UUID.randomUUID().toString();
        notifications.put(notificationId, new NotificationStatus());

        // 1시간 후 자동 삭제
        scheduler.schedule(() -> notifications.remove(notificationId), 1, TimeUnit.HOURS);

        return notificationId;
    }

    // 알림 읽음 상태 업데이트
    public static void markAsRead(String notificationId) {
        NotificationStatus status = notifications.get(notificationId);
        if (status != null) {
            status.setRead(true);
        }
    }

    // 알림 상태 조회
    public static NotificationStatus getStatus(String notificationId) {
        return notifications.get(notificationId);
    }
}
