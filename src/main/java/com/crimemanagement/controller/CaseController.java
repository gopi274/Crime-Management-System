package com.crimemanagement.controller;

import com.crimemanagement.dto.ApiResponse;
import com.crimemanagement.entity.Case;
import com.crimemanagement.service.InvestigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseController {

    private final InvestigationService investigationService;

    @Autowired
    public CaseController(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Case>>> getAllCases() {
        try {
            List<Case> cases = investigationService.getAllCases();
            return ResponseEntity.ok(ApiResponse.success("Cases retrieved successfully", cases));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve cases: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Case>> getCaseById(@PathVariable Integer id) {
        try {
            Case caseEntity = investigationService.getCaseById(id);
            if (caseEntity != null) {
                return ResponseEntity.ok(ApiResponse.success("Case retrieved successfully", caseEntity));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Case not found with ID: " + id));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error retrieving case: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Case>> createCase(@RequestBody Case caseEntity) {
        try {
            Case createdCase = investigationService.createCase(caseEntity);
            return ResponseEntity.ok(ApiResponse.success("Case created successfully", createdCase));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create case: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Case>> updateCase(@PathVariable Integer id, @RequestBody Case caseEntity) {
        try {
            caseEntity.setCaseId(id);
            Case updatedCase = investigationService.updateCase(caseEntity);
            return ResponseEntity.ok(ApiResponse.success("Case updated successfully", updatedCase));
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update case: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCase(@PathVariable Integer id) {
        try {
            boolean deleted = investigationService.deleteCase(id);
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success("Case deleted successfully", null));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Case not found"));
            }
        } catch (SQLException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete case: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Case>> updateCaseStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            Case updatedCase = investigationService.updateCaseStatus(id, status);
            if (updatedCase != null) {
                return ResponseEntity.ok(ApiResponse.success("Case status updated successfully", updatedCase));
            } else {
                return ResponseEntity.status(404).body(ApiResponse.error("Case not found"));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error updating status: " + e.getMessage()));
        }
    }

    @GetMapping("/assigned/{officerId}")
    public ResponseEntity<ApiResponse<List<Case>>> getAssignedCases(@PathVariable Integer officerId) {
        try {
            List<Case> cases = investigationService.getAssignedCases(officerId);
            return ResponseEntity.ok(ApiResponse.success("Assigned cases retrieved successfully", cases));
        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error retrieving assigned cases: " + e.getMessage()));
        }
    }
}
