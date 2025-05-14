package com.example.webrtc_signaling_server.controller;



import com.example.webrtc_signaling_server.model.*;
        import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.*;
        import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SignalingController {
    // Thread-safe collections for storing signaling data
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, SessionDescription> offers = new ConcurrentHashMap<>();
    private final Map<String, SessionDescription> answers = new ConcurrentHashMap<>();
    private final Map<String, List<Candidate>> callerCandidates = new ConcurrentHashMap<>();
    private final Map<String, List<Candidate>> receiverCandidates = new ConcurrentHashMap<>();
    // Add these new endpoints to SignalingController

    private final Map<String, CallNotification> notifications = new ConcurrentHashMap<>();

    @PostMapping("/notify/{roomId}")
    public ResponseEntity<Map<String, Boolean>> notifyCall(
            @PathVariable String roomId,
            @RequestBody CallNotification notification) {
        notification.setTimestamp(System.currentTimeMillis());
        notifications.put(roomId, notification);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/notification")
    public ResponseEntity<List<CallNotification>> getNotifications() {
        // Return all active notifications
        List<CallNotification> activeNotifications = notifications.values().stream()
                .filter(n -> System.currentTimeMillis() - n.getTimestamp() < 30000) // 30-second TTL
                .collect(Collectors.toList());
        return ResponseEntity.ok(activeNotifications);
    }

    @DeleteMapping("/notification/{roomId}")
    public ResponseEntity<Map<String, Boolean>> clearNotification(@PathVariable String roomId) {
        notifications.remove(roomId);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createRoom() {
        String roomId = UUID.randomUUID().toString().substring(0, 8);
        rooms.put(roomId, new Room());
        return ResponseEntity.ok(Collections.singletonMap("roomId", roomId));
    }

    @PostMapping("/offer/{roomId}")
    public ResponseEntity<Map<String, Boolean>> saveOffer(
            @PathVariable String roomId,
            @RequestBody SessionDescription offer) {
        offers.put(roomId, offer);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/offer/{roomId}")
    public ResponseEntity<SessionDescription> getOffer(@PathVariable String roomId) {
        return ResponseEntity.ok(offers.getOrDefault(roomId, new SessionDescription()));
    }

    @PostMapping("/answer/{roomId}")
    public ResponseEntity<Map<String, Boolean>> saveAnswer(
            @PathVariable String roomId,
            @RequestBody SessionDescription answer) {
        answers.put(roomId, answer);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/answer/{roomId}")
    public ResponseEntity<SessionDescription> getAnswer(@PathVariable String roomId) {
        return ResponseEntity.ok(answers.getOrDefault(roomId, new SessionDescription()));
    }

    @PostMapping("/candidate")
    public ResponseEntity<Map<String, Boolean>> saveCandidate(@RequestBody SignalingMessage message) {
        Map<String, List<Candidate>> targetMap = "caller".equals(message.getType()) ?
                callerCandidates : receiverCandidates;

        targetMap.computeIfAbsent(message.getRoomId(), k -> new ArrayList<>())
                .add(message.getCandidate());

        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/candidates/{roomId}/{type}")
    public ResponseEntity<List<Candidate>> getCandidates(
            @PathVariable String roomId,
            @PathVariable String type) {
        List<Candidate> candidates = "caller".equals(type) ?
                callerCandidates.getOrDefault(roomId, new ArrayList<>()) :
                receiverCandidates.getOrDefault(roomId, new ArrayList<>());

        // Clear candidates after retrieval
        if ("caller".equals(type)) {
            callerCandidates.remove(roomId);
        } else {
            receiverCandidates.remove(roomId);
        }

        return ResponseEntity.ok(candidates);
    }

    @PostMapping("/end/{roomId}")
    public ResponseEntity<Map<String, Boolean>> endSession(@PathVariable String roomId) {
        rooms.remove(roomId);
        offers.remove(roomId);
        answers.remove(roomId);
        callerCandidates.remove(roomId);
        receiverCandidates.remove(roomId);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}