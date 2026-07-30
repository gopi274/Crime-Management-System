package com.crimemanagement.controller;

import com.crimemanagement.dto.ApiResponse;
import com.crimemanagement.entity.FIR;
import com.crimemanagement.service.FIRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/firs")
@CrossOrigin(origins = "*") // For development
public class FIRController {

    private final FIRService firService;

    @Autowired
    public FIRController(FIRService firService) {
        this.firService = firService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FIR>>> getAllFIRs() {
        try {
            List<FIR> firs = firService.getAllFIRs();
            return ResponseEntity.ok(ApiResponse.success("FIRs retrieved successfully", firs));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve FIRs: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FIR>> getFIRById(@PathVariable Integer id) {
        try {
            FIR fir = firService.getFIRById(id);
            if (fir != null) {
                return ResponseEntity.ok(ApiResponse.success("FIR retrieved successfully", fir));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("FIR not found with ID: " + id));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error retrieving FIR: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FIR>> createFIR(@RequestBody FIR fir) {
        try {
            FIR createdFIR = firService.createFIR(fir);
            return ResponseEntity.ok(ApiResponse.success("FIR created successfully", createdFIR));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create FIR: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FIR>> updateFIR(@PathVariable Integer id, @RequestBody FIR fir) {
        try {
            fir.setFirId(id);
            FIR updatedFIR = firService.updateFIR(fir);
            return ResponseEntity.ok(ApiResponse.success("FIR updated successfully", updatedFIR));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update FIR: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFIR(@PathVariable Integer id) {
        try {
            boolean deleted = firService.deleteFIR(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("FIR deleted successfully", null));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("FIR not found"));
            }
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete FIR: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<FIR>>> searchFIRs(@RequestParam String keyword) {
        try {
            List<FIR> firs = firService.searchFIRsByKeyword(keyword);
            return ResponseEntity.ok(ApiResponse.success("Search results for: " + keyword, firs));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }
}
