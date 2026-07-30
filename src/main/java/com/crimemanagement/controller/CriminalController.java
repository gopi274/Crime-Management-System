package com.crimemanagement.controller;

import com.crimemanagement.dto.ApiResponse;
import com.crimemanagement.entity.Criminal;
import com.crimemanagement.service.CriminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/criminals")
@CrossOrigin(origins = "*")
public class CriminalController {

    private final CriminalService criminalService;

    @Autowired
    public CriminalController(CriminalService criminalService) {
        this.criminalService = criminalService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Criminal>>> getAllCriminals() {
        try {
            List<Criminal> criminals = criminalService.getAllCriminals();
            return ResponseEntity.ok(ApiResponse.success("Criminals retrieved successfully", criminals));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve criminals: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Criminal>> getCriminalById(@PathVariable Integer id) {
        try {
            Criminal criminal = criminalService.getCriminalById(id);
            if (criminal != null) {
                return ResponseEntity.ok(ApiResponse.success("Criminal retrieved successfully", criminal));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Criminal not found with ID: " + id));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error retrieving criminal: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Criminal>> createCriminal(@RequestBody Criminal criminal) {
        try {
            Criminal createdCriminal = criminalService.createCriminal(criminal);
            return ResponseEntity.ok(ApiResponse.success("Criminal created successfully", createdCriminal));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create criminal: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Criminal>> updateCriminal(@PathVariable Integer id,
            @RequestBody Criminal criminal) {
        try {
            criminal.setCriminalId(id);
            Criminal updatedCriminal = criminalService.updateCriminal(criminal);
            return ResponseEntity.ok(ApiResponse.success("Criminal updated successfully", updatedCriminal));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update criminal: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCriminal(@PathVariable Integer id) {
        try {
            boolean deleted = criminalService.deleteCriminal(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Criminal deleted successfully", null));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Criminal not found"));
            }
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete criminal: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Criminal>>> searchCriminals(@RequestParam String name) {
        try {
            List<Criminal> criminals = criminalService.getCriminalsByName(name);
            return ResponseEntity.ok(ApiResponse.success("Search results for: " + name, criminals));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }
}
