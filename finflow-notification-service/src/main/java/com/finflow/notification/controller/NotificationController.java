package com.finflow.notification.controller;

import com.finflow.notification.entity.Notification;
import com.finflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ─── Get notifications for an account ───
    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<Notification>> getNotifications(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByAccount(accountNumber));
    }

    // ─── Health Check ───
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running!");
    }

}
