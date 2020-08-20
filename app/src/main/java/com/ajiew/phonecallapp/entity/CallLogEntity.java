package com.ajiew.phonecallapp.entity;

import android.provider.CallLog;

import java.io.Serializable;

public class CallLogEntity implements Serializable {
    private int id;
    private String name;
    private String phoneNumber;
    private int type;
    private long date;
    private long duration;
    private boolean isChecked;
    private int offHook;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "CallEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", type=" + getTypeStr(type) +
                ", data=" + date +
                ", duration=" + duration +
                ", isChecked=" + isChecked +
                '}';
    }

    public static String getTypeStr(int type) {
        if (CallLog.Calls.INCOMING_TYPE == type) { // 1
            return "来电";
        } else if (CallLog.Calls.OUTGOING_TYPE == type) { // 2
            return "去电";
        } else if (CallLog.Calls.MISSED_TYPE == type) { // 3
            return "未接";
        } else if (CallLog.Calls.VOICEMAIL_TYPE == type) { // 4
            return "语音邮件";
        } else if (CallLog.Calls.REJECTED_TYPE == type) { // 5
            return "拒绝";
        } else if (CallLog.Calls.BLOCKED_TYPE == type) { // 6
            return "阻止";
        } else {
            return "未知";
        }
    }

    public static boolean isOutgoing(int type) {
        return CallLog.Calls.OUTGOING_TYPE == type;
    }
}
