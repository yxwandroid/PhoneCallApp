package com.ajiew.phonecallapp.phonecallui;

public class MessageEvent {

    private String CallState;

    public MessageEvent(String callState) {
        CallState = callState;
    }

    public String getCallState() {
        return CallState;
    }

    public void setCallState(String callState) {
        CallState = callState;
    }
}