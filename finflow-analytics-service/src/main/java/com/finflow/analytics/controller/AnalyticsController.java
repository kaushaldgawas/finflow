package com.finflow.analytics.controller;

import com.finflow.analytics.dto.AnalyticsResponse;
import com.finflow.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ─── Get Analytics for an Account ───
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AnalyticsResponse> getAccountAnalytics(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(
                analyticsService.getAccountAnalytics(accountNumber));
    }

    // ─── Invalidate Cache (call after new transaction) ───
    @DeleteMapping("/cache/{accountNumber}")
    public ResponseEntity<String> invalidateCache(
            @PathVariable String accountNumber) {
        analyticsService.invalidateCache(accountNumber);
        return ResponseEntity.ok("Cache invalidated for: " + accountNumber);
    }

    // ─── Health Check ───
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Service is running!");
    }

}
