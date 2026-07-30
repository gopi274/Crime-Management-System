package com.crimemanagement.controller;

import com.crimemanagement.dto.ApiResponse;
import com.crimemanagement.entity.Criminal;
import com.crimemanagement.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardService.DashboardStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved", dashboardService.getStats()));
    }

    @GetMapping("/recent-criminals")
    public ResponseEntity<ApiResponse<List<Criminal>>> getRecentCriminals(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity
                .ok(ApiResponse.success("Recent criminals retrieved", dashboardService.getRecentCriminals(limit)));
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse<List<String>>> getRecentActivity(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity
                .ok(ApiResponse.success("Recent activity retrieved", dashboardService.getRecentActivity(limit)));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTrendData() {
        return ResponseEntity.ok(ApiResponse.success("Trend data retrieved", dashboardService.getTrendData()));
    }
}
