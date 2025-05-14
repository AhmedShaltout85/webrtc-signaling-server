package com.example.webrtc_signaling_server.model;



import lombok.Data;

@Data
public class CallNotification {
    private String roomId;
    private boolean isCalling;
    private long timestamp;
}