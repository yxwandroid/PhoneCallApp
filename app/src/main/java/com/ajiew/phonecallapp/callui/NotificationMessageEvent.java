package com.ajiew.phonecallapp.callui;

public class NotificationMessageEvent {

    public NotificationState mNotificationState;

    public NotificationMessageEvent(NotificationState notificationState) {
        mNotificationState = notificationState;
    }


    public NotificationState getmNotificationState() {
        return mNotificationState;
    }

    public void setmNotificationState(NotificationState mNotificationState) {
        this.mNotificationState = mNotificationState;
    }
}

