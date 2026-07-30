package com.crimemanagement.dao;

import com.crimemanagement.entity.Case;
import com.crimemanagement.util.DatabaseConnection;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

@Repository
public class CaseDAO implements BaseDAO<Case, Integer> {
    @Override
    public Case create(Case caseEntity) throws SQLException {
        String sql = "INSERT INTO cases (case_number, fir_id, case_title, case_description, priority, status, assigned_officer_id, court_id, target_date, delay_explanation, next_trial_date, witness_summons, is_public, verdict_text, article_text, is_verified_by_court) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, caseEntity.getCaseNumber());
            statement.setInt(2, caseEntity.getFirId());
            statement.setString(3, caseEntity.getCaseTitle());
            statement.setString(4, caseEntity.getCaseDescription());
            statement.setString(5, caseEntity.getPriority());
            statement.setString(6, caseEntity.getStatus());
            statement.setObject(7, caseEntity.getAssignedOfficerId());
            statement.setObject(8, caseEntity.getCourtId());
            statement.setDate(9, caseEntity.getTargetDate() != null ? Date.valueOf(caseEntity.getTargetDate()) : null);
            statement.setString(10, caseEntity.getDelayExplanation());
            statement.setDate(11, caseEntity.getNextTrialDate() != null ? Date.valueOf(caseEntity.getNextTrialDate()) : null);
            statement.setString(12, caseEntity.getWitnessSummons());
            statement.setBoolean(13, caseEntity.isPublic());
            statement.setString(14, caseEntity.getVerdictText());
            statement.setString(15, caseEntity.getArticleText());
            statement.setBoolean(16, caseEntity.isVerifiedByCourt());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating case failed.");

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) caseEntity.setCaseId(generatedKeys.getInt(1));
            }
            return caseEntity;
        }
    }

    @Override
    public Case findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM cases WHERE case_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapResultSetToCase(resultSet);
            }
        }
        return null;
    }

    @Override
    public List<Case> findAll() throws SQLException {
        return findAll(null, null);
    }

    public List<Case> findAll(Integer stationId, Integer courtId) throws SQLException {
        String sql = "SELECT c.* FROM cases c";
        List<Integer> params = new ArrayList<>();
        
        if (stationId != null) {
            sql += " JOIN firs f ON c.fir_id = f.fir_id WHERE f.station_id = ?";
            params.add(stationId);
        } else if (courtId != null) {
            sql += " WHERE c.court_id = ?";
            params.add(courtId);
        }
        
        sql += " ORDER BY c.created_at DESC";
        
        List<Case> cases = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                statement.setInt(i + 1, params.get(i));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) cases.add(mapResultSetToCase(resultSet));
            }
        }
        return cases;
    }

    public List<Case> findByCourtId(Integer courtId) throws SQLException {
        return findAll(null, courtId);
    }

    @Override
    public Case update(Case caseEntity) throws SQLException {
        String sql = "UPDATE cases SET case_number = ?, fir_id = ?, case_title = ?, case_description = ?, priority = ?, status = ?, assigned_officer_id = ?, court_id = ?, target_date = ?, delay_explanation = ?, next_trial_date = ?, witness_summons = ?, is_public = ?, verdict_text = ?, article_text = ?, is_verified_by_court = ? WHERE case_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, caseEntity.getCaseNumber());
            statement.setInt(2, caseEntity.getFirId());
            statement.setString(3, caseEntity.getCaseTitle());
            statement.setString(4, caseEntity.getCaseDescription());
            statement.setString(5, caseEntity.getPriority());
            statement.setString(6, caseEntity.getStatus());
            statement.setObject(7, caseEntity.getAssignedOfficerId());
            statement.setObject(8, caseEntity.getCourtId());
            statement.setDate(9, caseEntity.getTargetDate() != null ? Date.valueOf(caseEntity.getTargetDate()) : null);
            statement.setString(10, caseEntity.getDelayExplanation());
            statement.setDate(11, caseEntity.getNextTrialDate() != null ? Date.valueOf(caseEntity.getNextTrialDate()) : null);
            statement.setString(12, caseEntity.getWitnessSummons());
            statement.setBoolean(13, caseEntity.isPublic());
            statement.setString(14, caseEntity.getVerdictText());
            statement.setString(15, caseEntity.getArticleText());
            statement.setBoolean(16, caseEntity.isVerifiedByCourt());
            statement.setInt(17, caseEntity.getCaseId());

            statement.executeUpdate();
            return caseEntity;
        }
    }

    @Override
    public boolean deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM cases WHERE case_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        String sql = "SELECT 1 FROM cases WHERE case_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cases";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) return resultSet.getLong(1);
            return 0;
        }
    }

    public List<Case> findAllPublic() throws SQLException {
        String sql = "SELECT * FROM cases WHERE is_public = TRUE ORDER BY updated_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) cases.add(mapResultSetToCase(resultSet));
        }
        return cases;
    }

    public Case findByCaseNumber(String caseNumber) throws SQLException {
        String sql = "SELECT * FROM cases WHERE case_number = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, caseNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return mapResultSetToCase(resultSet);
            }
        }
        return null;
    }

    public List<Case> findByFirId(Integer firId) throws SQLException {
        String sql = "SELECT * FROM cases WHERE fir_id = ? ORDER BY created_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, firId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) cases.add(mapResultSetToCase(resultSet));
            }
        }
        return cases;
    }

    public List<Case> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM cases WHERE status = ? ORDER BY created_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) cases.add(mapResultSetToCase(resultSet));
            }
        }
        return cases;
    }

    public List<Case> findByPriority(String priority) throws SQLException {
        String sql = "SELECT * FROM cases WHERE priority = ? ORDER BY created_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, priority);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) cases.add(mapResultSetToCase(resultSet));
            }
        }
        return cases;
    }

    public List<Case> findByAssignedOfficer(Integer officerId) throws SQLException {
        String sql = "SELECT * FROM cases WHERE assigned_officer_id = ? ORDER BY created_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, officerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) cases.add(mapResultSetToCase(resultSet));
            }
        }
        return cases;
    }

    public List<Case> searchByKeyword(String keyword) throws SQLException {
        String sql = "SELECT * FROM cases WHERE case_title LIKE ? OR case_description LIKE ? ORDER BY created_at DESC";
        List<Case> cases = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) cases.add(mapResultSetToCase(resultSet));
            }
        }
        return cases;
    }

    private Case mapResultSetToCase(ResultSet resultSet) throws SQLException {
        Case caseEntity = new Case();
        caseEntity.setCaseId(resultSet.getInt("case_id"));
        caseEntity.setCaseNumber(resultSet.getString("case_number"));
        caseEntity.setFirId(resultSet.getInt("fir_id"));
        caseEntity.setCaseTitle(resultSet.getString("case_title"));
        caseEntity.setCaseDescription(resultSet.getString("case_description"));
        caseEntity.setPriority(resultSet.getString("priority"));
        caseEntity.setStatus(resultSet.getString("status"));
        caseEntity.setAssignedOfficerId((Integer) resultSet.getObject("assigned_officer_id"));
        
        caseEntity.setCourtId((Integer) resultSet.getObject("court_id"));
        
        Date targetDate = resultSet.getDate("target_date");
        if (targetDate != null) caseEntity.setTargetDate(targetDate.toLocalDate());
        
        caseEntity.setDelayExplanation(resultSet.getString("delay_explanation"));
        
        Date nextTrialDate = resultSet.getDate("next_trial_date");
        if (nextTrialDate != null) caseEntity.setNextTrialDate(nextTrialDate.toLocalDate());
        
        caseEntity.setWitnessSummons(resultSet.getString("witness_summons"));
        caseEntity.setIsPublic(resultSet.getBoolean("is_public"));
        caseEntity.setVerdictText(resultSet.getString("verdict_text"));
        caseEntity.setArticleText(resultSet.getString("article_text"));
        caseEntity.setIsVerifiedByCourt(resultSet.getBoolean("is_verified_by_court"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) caseEntity.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) caseEntity.setUpdatedAt(updatedAt.toLocalDateTime());

        return caseEntity;
    }

    public class CaseRecord {
        private int caseId;
        private String caseNumber;
        private int firId;
        private String caseTitle;
        private String caseDescription;
        private String priority;
        private String status;
        private Integer assignedOfficerId; 
        private Integer courtId;
        private LocalDate targetDate; 
        private String delayExplanation;
        private LocalDate nextTrialDate;
        private String witnessSummons;

        public int getCaseId() { return caseId; }
        public void setCaseId(int caseId) { this.caseId = caseId; }
        public String getCaseNumber() { return caseNumber; }
        public void setCaseNumber(String caseNumber) { this.caseNumber = caseNumber; }
        public int getFirId() { return firId; }
        public void setFirId(int firId) { this.firId = firId; }
        public String getCaseTitle() { return caseTitle; }
        public void setCaseTitle(String caseTitle) { this.caseTitle = caseTitle; }
        public String getCaseDescription() { return caseDescription; }
        public void setCaseDescription(String caseDescription) { this.caseDescription = caseDescription; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getAssignedOfficerId() { return assignedOfficerId; }
        public void setAssignedOfficerId(Integer assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }
        public Integer getCourtId() { return courtId; }
        public void setCourtId(Integer courtId) { this.courtId = courtId; }
        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
        public String getDelayExplanation() { return delayExplanation; }
        public void setDelayExplanation(String delayExplanation) { this.delayExplanation = delayExplanation; }
        public LocalDate getNextTrialDate() { return nextTrialDate; }
        public void setNextTrialDate(LocalDate nextTrialDate) { this.nextTrialDate = nextTrialDate; }
        public String getWitnessSummons() { return witnessSummons; }
        public void setWitnessSummons(String witnessSummons) { this.witnessSummons = witnessSummons; }
    }

    public CaseRecord addCase(CaseRecord caseRecord) throws SQLException {
        Case c = new Case();
        c.setCaseNumber(caseRecord.getCaseNumber());
        c.setFirId(caseRecord.getFirId());
        c.setCaseTitle(caseRecord.getCaseTitle());
        c.setCaseDescription(caseRecord.getCaseDescription());
        c.setPriority(caseRecord.getPriority());
        c.setStatus(caseRecord.getStatus());
        c.setAssignedOfficerId(caseRecord.getAssignedOfficerId());
        c.setCourtId(caseRecord.getCourtId());
        c.setTargetDate(caseRecord.getTargetDate());
        c.setDelayExplanation(caseRecord.getDelayExplanation());
        c.setNextTrialDate(caseRecord.getNextTrialDate());
        c.setWitnessSummons(caseRecord.getWitnessSummons());
        
        Case created = create(c);
        caseRecord.setCaseId(created.getCaseId());
        return caseRecord;
    }

    public boolean removeCase(int caseId) throws SQLException {
        return deleteById(caseId);
    }

    public long countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cases WHERE status = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getLong(1);
            }
        }
        return 0;
    }

    public long countByPriority(String priority) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cases WHERE priority = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, priority);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getLong(1);
            }
        }
        return 0;
    }

    public java.util.Map<String, Long> getMonthlyCaseCounts(int months) throws SQLException {
        YearMonth cutoff = YearMonth.now().minusMonths(Math.max(months - 1, 0));
        LinkedHashMap<String, Long> stats = new LinkedHashMap<>();

        for (Case caseEntity : findAll()) {
            if (caseEntity.getCreatedAt() == null) {
                continue;
            }
            YearMonth caseMonth = YearMonth.from(caseEntity.getCreatedAt());
            if (caseMonth.isBefore(cutoff)) {
                continue;
            }
            stats.merge(caseMonth.toString(), 1L, (oldVal, newVal) -> oldVal + newVal);
        }

        return stats.entrySet().stream()
                .sorted(Comparator.comparing(java.util.Map.Entry::getKey))
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }
}
