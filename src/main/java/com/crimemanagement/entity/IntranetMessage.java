package com.crimemanagement.entity;

import java.time.LocalDateTime;

public class IntranetMessage {
    private Integer msgId;
    private Integer senderId;
    private Integer receiverId;
    private String body;
    private Boolean isBolo;
    private String status = "PENDING";
    private LocalDateTime sentAt;
    
    // Virtual fields
    private String senderName;
    private String senderRank;

    public Integer getMsgId() { return msgId; }
    public void setMsgId(Integer msgId) { this.msgId = msgId; }
    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }
    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Boolean getIsBolo() { return isBolo; }
    public void setIsBolo(Boolean isBolo) { this.isBolo = isBolo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderRank() { return senderRank; }
    public void setSenderRank(String senderRank) { this.senderRank = senderRank; }
}
