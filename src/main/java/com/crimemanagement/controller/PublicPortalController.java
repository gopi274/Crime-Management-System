package com.crimemanagement.controller;

import com.crimemanagement.service.FIRService;
import com.crimemanagement.service.InvestigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*") // For development web portal
public class PublicPortalController {

    private final InvestigationService investigationService;
    private final FIRService firService;
    private final com.crimemanagement.service.OfficerService officerService;

    @Autowired
    public PublicPortalController(InvestigationService investigationService, FIRService firService, com.crimemanagement.service.OfficerService officerService) {
        this.investigationService = investigationService;
        this.firService = firService;
        this.officerService = officerService;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getGlobalStats() {
        try {
            long totalCases = investigationService.getAllCases().size();
            long closedCases = investigationService.getAllCases().stream()
                .filter(c -> "Closed".equalsIgnoreCase(c.getStatus()) || "Solved".equalsIgnoreCase(c.getStatus()))
                .count();
                
            long openCases = totalCases - closedCases;
            long activeOfficers = officerService.getOnDutyCount();
            long totalStations = new com.crimemanagement.dao.PoliceStationDAO().findAll().size();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("openCases", openCases);
            stats.put("closedCases", closedCases);
            stats.put("activeOfficers", activeOfficers);
            stats.put("stationsOnline", totalStations);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stories")
    public ResponseEntity<?> getFeaturedStories() {
        try {
            com.crimemanagement.dao.CaseDAO caseDao = new com.crimemanagement.dao.CaseDAO();
            // Filter only cases verified by court and closed/solved for public publication
            List<com.crimemanagement.entity.Case> publicCases = caseDao.findAll().stream()
                .filter(c -> c.isVerifiedByCourt())
                .filter(c -> "Closed".equalsIgnoreCase(c.getStatus()) || "Solved".equalsIgnoreCase(c.getStatus()))
                .toList();
            
            List<Map<String, String>> stories = publicCases.stream()
                .map(c -> Map.of(
                    "id", String.valueOf(c.getCaseId()),
                    "title", c.getCaseTitle(),
                    "summary", (c.getArticleText() != null && !c.getArticleText().isEmpty() ? c.getArticleText().substring(0, Math.min(200, c.getArticleText().length())) + "..." : c.getCaseDescription()),
                    "date", c.getUpdatedAt() != null ? c.getUpdatedAt().toString().substring(0, 10) : "2026-04-01",
                    "status", c.getStatus()
                ))
                .toList();
            return ResponseEntity.ok(stories);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); 
        }
    }

    @GetMapping("/case-details/{caseId}")
    public ResponseEntity<?> getCaseDetails(@PathVariable int caseId) {
        try {
            com.crimemanagement.dao.CaseDAO caseDao = new com.crimemanagement.dao.CaseDAO();
            com.crimemanagement.entity.Case c = caseDao.findById(caseId);
            if (c != null && c.isPublic()) {
                Map<String, Object> details = new HashMap<>();
                details.put("caseNumber", c.getCaseNumber());
                details.put("title", c.getCaseTitle());
                details.put("description", c.getCaseDescription());
                details.put("status", c.getStatus());
                details.put("verdict", c.getVerdictText());
                details.put("article", c.getArticleText());
                details.put("date", c.getUpdatedAt());
                details.put("charges", "Charges applied as per Bharatiya Nyaya Sanhita (BNS) 2023 provisions.");
                return ResponseEntity.ok(details);
            }
            return ResponseEntity.status(404).body(Map.of("error", "Case not found or not published."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/approve-verdict/{caseId}")
    public ResponseEntity<?> approveVerdict(@PathVariable int caseId, @RequestBody Map<String, String> payload) {
        // In a real system, security would be checking Admin role via JWT token here.
        try {
            com.crimemanagement.dao.CaseDAO caseDao = new com.crimemanagement.dao.CaseDAO();
            com.crimemanagement.entity.Case c = caseDao.findById(caseId);
            if (c != null) {
                c.setIsPublic(true);
                c.setVerdictText(payload.get("verdict"));
                caseDao.update(c);
                return ResponseEntity.ok(Map.of("message", "Verdict successfully published to the Citizen Portal."));
            }
            return ResponseEntity.status(404).body(Map.of("error", "Case not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/complaints")
    public ResponseEntity<?> submitComplaint(@RequestBody Map<String, String> payload) {
        try {
            String trackingId = "TRACK-" + System.currentTimeMillis();
            
            com.crimemanagement.entity.Complaint c = new com.crimemanagement.entity.Complaint();
            c.setDescription(payload.get("description"));
            c.setLocation(payload.get("location"));
            c.setTrackingId(trackingId);
            
            com.crimemanagement.service.ComplaintAssignmentService aiRouter = new com.crimemanagement.service.ComplaintAssignmentService();
            String routeResult = aiRouter.parseAndAssign(c);
            
            com.crimemanagement.dao.ComplaintDAO complaintDAO = new com.crimemanagement.dao.ComplaintDAO();
            complaintDAO.insert(c);
            
            String translatedStatus = translateStatus(routeResult);
            return ResponseEntity.ok(Map.of(
                "message", "Complaint securely recorded. Current Status: " + translatedStatus, 
                "trackingId", trackingId,
                "routingConfidence", routeResult.contains("MANUAL") || routeResult.contains("PENDING") ? 0.60 : 0.98
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "Processing";
        switch (status) {
            case "EMERGENCY_DISPATCH_112":
                return "🚨 Emergency Dispatch (112) Informed";
            case "AUTO_ASSIGNED_TVM_CITY":
                return "Forwarded to Trivandrum City Police Station";
            case "AUTO_ASSIGNED_KOCHI_CITY":
                return "Forwarded to Kochi City Police Station";
            case "AUTO_ASSIGNED_KOZHIKODE_CITY":
                return "Forwarded to Kozhikode City Police Station";
            case "PENDING_MANUAL_REVIEW":
                return "Under Officer Review for Station Assignment";
            default:
                return status.replace("_", " ").toLowerCase();
        }
    }

    @GetMapping("/track/{*id}")
    public ResponseEntity<?> trackStatus(@PathVariable String id) {
        if (id != null && id.startsWith("/")) id = id.substring(1); // Strip leading slash from capture
        try {
            com.crimemanagement.entity.FIR fir = firService.getFIRByNumber(id);
            com.crimemanagement.dao.CaseDAO caseDao = new com.crimemanagement.dao.CaseDAO();
            
            if (fir != null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("type", "FIR");
                resp.put("id", fir.getFirNumber());
                resp.put("status", fir.getStatus());
                resp.put("description", fir.getCrimeDescription());
                resp.put("date", fir.getDateOfIncident());
                
                List<com.crimemanagement.entity.Case> cases = caseDao.findByFirId(fir.getFirId());
                if (cases != null && !cases.isEmpty()) {
                    resp.put("caseNumber", cases.get(0).getCaseNumber());
                    resp.put("caseStatus", cases.get(0).getStatus());
                    resp.put("nextTrialDate", cases.get(0).getNextTrialDate());
                }
                return ResponseEntity.ok(resp);
            }
            
            com.crimemanagement.entity.Case c = caseDao.findByCaseNumber(id);
            if (c != null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("type", "CASE");
                resp.put("id", c.getCaseNumber());
                resp.put("status", c.getStatus());
                resp.put("description", c.getCaseDescription());
                resp.put("nextTrialDate", c.getNextTrialDate());
                return ResponseEntity.ok(resp);
            }
            
            com.crimemanagement.dao.ComplaintDAO complaintDAO = new com.crimemanagement.dao.ComplaintDAO();
            com.crimemanagement.entity.Complaint complaint = complaintDAO.findByTrackingId(id);
            if (complaint != null) {
                return ResponseEntity.ok(Map.of(
                    "type", "Sent to Police Station",
                    "id", id,
                    "status", translateStatus(complaint.getStatus() != null ? complaint.getStatus() : "Under Review"),
                    "description", "Your complaint is currently undergoing official verification."
                ));
            }

            return ResponseEntity.status(404).body(Map.of("error", "No records found for ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> citizenLogin(@RequestBody Map<String, String> payload) {
        try {
            com.crimemanagement.service.AuthService auth = new com.crimemanagement.service.AuthService();
            com.crimemanagement.entity.UserAccount user = auth.authenticate(payload.get("username"), payload.get("password"));
            if (user != null && "CITIZEN".equals(user.getRole())) {
                return ResponseEntity.ok(Map.of(
                    "token", "CITIZEN-JWT-TOKEN-" + user.getUserId(),
                    "citizenId", user.getCitizenId(),
                    "username", user.getUsername()
                ));
            }
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials or unauthorized role."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-firs/{citizenId}")
    public ResponseEntity<?> getMyFIRs(@PathVariable int citizenId) {
        try {
            List<com.crimemanagement.entity.FIR> all = firService.getAllFIRs();
            List<com.crimemanagement.entity.FIR> mine = all.stream()
                .filter(f -> f.getCitizenId() != null && f.getCitizenId() == citizenId)
                .toList();
            return ResponseEntity.ok(mine);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
