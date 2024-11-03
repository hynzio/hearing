package org.hdmd.hearingdemo.service;


public class NotificationStatus {
    private boolean isRead = false;
    private int resendCount = 0;

    // Getter, Setter
    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getResendCount() {
        return resendCount;
    }

    public void incrementResendCount() {
        this.resendCount++;
    }
}
