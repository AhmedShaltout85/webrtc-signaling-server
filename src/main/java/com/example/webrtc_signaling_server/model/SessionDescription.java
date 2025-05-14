package com.example.webrtc_signaling_server.model;



import lombok.Data;

@Data
public class SessionDescription {
    private String sdp;
    private String type;
}