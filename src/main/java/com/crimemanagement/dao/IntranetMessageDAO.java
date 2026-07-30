package com.crimemanagement.dao;

import com.crimemanagement.entity.IntranetMessage;
import com.crimemanagement.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IntranetMessageDAO {

    public void create(IntranetMessage msg) throws SQLException {
        String sql = "INSERT INTO intranet_messages (sender_id, receiver_id, body, is_bolo, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, msg.getSenderId());
            if (msg.getReceiverId() != null) ps.setInt(2, msg.getReceiverId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, msg.getBody());
            ps.setBoolean(4, msg.getIsBolo() != null && msg.getIsBolo());
            ps.setString(5, msg.getStatus() != null ? msg.getStatus() : "PENDING");
            ps.executeUpdate();
        }
    }

    public void updateStatus(int msgId, String status) throws SQLException {
        String sql = "UPDATE intranet_messages SET status = ? WHERE msg_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, msgId);
            ps.executeUpdate();
        }
    }

    public List<IntranetMessage> getFeed(Integer currentUserId) throws SQLException {
        // Feed gets all active BOLOs + Messages targeted directly to currentUserId
        String sql = "SELECT i.*, u.username as sender_name, u.role as sender_rank " +
                     "FROM intranet_messages i " +
                     "JOIN users u ON i.sender_id = u.user_id " +
                     "WHERE i.is_bolo = TRUE OR i.receiver_id = ? " +
                     "ORDER BY i.sent_at DESC";
        
        List<IntranetMessage> list = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (currentUserId != null) {
                ps.setInt(1, currentUserId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    IntranetMessage m = new IntranetMessage();
                    m.setMsgId(rs.getInt("msg_id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    m.setReceiverId((Integer) rs.getObject("receiver_id"));
                    m.setBody(rs.getString("body"));
                    m.setIsBolo(rs.getBoolean("is_bolo"));
                    m.setStatus(rs.getString("status"));
                    
                    Timestamp ts = rs.getTimestamp("sent_at");
                    if (ts != null) m.setSentAt(ts.toLocalDateTime());
                    
                    m.setSenderName(rs.getString("sender_name"));
                    m.setSenderRank(rs.getString("sender_rank"));
                    list.add(m);
                }
            }
        }
        return list;
    }
}
