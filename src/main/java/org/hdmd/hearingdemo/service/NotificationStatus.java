package org.hdmd.hearingdemo.service;

import lombok.Data;

@Data
public class NotificationStatus {
    private boolean isRead = false;
    private int resendCount = 0;

    public void incrementResendCount() {
        this.resendCount++;
    }
}