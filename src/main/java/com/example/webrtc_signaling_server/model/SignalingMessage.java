package com.example.webrtc_signaling_server.model;


import lombok.Data;

@Data
public class SignalingMessage {
    private String roomId;
    private Candidate candidate;
    private String type; // "caller" or "receiver"
}