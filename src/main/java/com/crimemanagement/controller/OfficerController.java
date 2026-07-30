package com.crimemanagement.controller;

import com.crimemanagement.dto.ApiResponse;
import com.crimemanagement.entity.Officer;
import com.crimemanagement.service.OfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/officers")
@CrossOrigin(origins = "*")
public class OfficerController {

    private final OfficerService officerService;

    @Autowired
    public OfficerController(OfficerService officerService) {
        this.officerService = officerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Officer>>> getAllOfficers() {
        try {
            List<Officer> officers = officerService.getAllOfficers();
            return ResponseEntity.ok(ApiResponse.success("Officers retrieved successfully", officers));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve officers: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Officer>> getOfficerById(@PathVariable Integer id) {
        try {
            Officer officer = officerService.getOfficerById(id);
            if (officer != null) {
                return ResponseEntity.ok(ApiResponse.success("Officer retrieved successfully", officer));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Officer not found with ID: " + id));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error retrieving officer: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Officer>> createOfficer(@RequestBody Officer officer) {
        try {
            Officer createdOfficer = officerService.createOfficer(officer);
            return ResponseEntity.ok(ApiResponse.success("Officer created successfully", createdOfficer));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create officer: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Officer>> updateOfficer(@PathVariable Integer id, @RequestBody Officer officer) {
        try {
            officer.setOfficerId(id);
            Officer updatedOfficer = officerService.updateOfficer(officer);
            return ResponseEntity.ok(ApiResponse.success("Officer updated successfully", updatedOfficer));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update officer: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOfficer(@PathVariable Integer id) {
        try {
            boolean deleted = officerService.deleteOfficer(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Officer deleted successfully", null));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Officer not found"));
            }
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete officer: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Officer>>> searchOfficers(@RequestParam String name) {
        try {
            List<Officer> officers = officerService.searchOfficersByName(name);
            return ResponseEntity.ok(ApiResponse.success("Search results for: " + name, officers));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/badge/{badgeNumber}")
    public ResponseEntity<ApiResponse<Officer>> getOfficerByBadge(@PathVariable String badgeNumber) {
        try {
            Officer officer = officerService.getOfficerByBadgeNumber(badgeNumber);
            if (officer != null) {
                return ResponseEntity.ok(ApiResponse.success("Officer retrieved successfully", officer));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Officer not found with badge: " + badgeNumber));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error retrieving officer: " + e.getMessage()));
        }
    }
}
