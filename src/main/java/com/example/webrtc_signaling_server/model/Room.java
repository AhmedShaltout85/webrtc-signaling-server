package com.example.webrtc_signaling_server.model;


import lombok.Data;

import java.time.Instant;

@Data
public class Room {
    private Instant createdAt;

    public Room() {
        this.createdAt = Instant.now();
    }
}