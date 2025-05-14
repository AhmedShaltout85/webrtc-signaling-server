package com.example.webrtc_signaling_server.model;



import lombok.Data;

@Data
public class Candidate {
    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
}