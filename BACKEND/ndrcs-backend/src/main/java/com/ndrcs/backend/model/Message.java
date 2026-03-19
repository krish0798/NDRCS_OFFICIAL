package com.ndrcs.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private SenderReceiverRole senderRole;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_role", nullable = false)
    private SenderReceiverRole receiverRole;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.CUSTOM;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_deleted_by_sender", nullable = false)
    private boolean isDeletedBySender = false;

    @Column(name = "is_deleted_by_receiver", nullable = false)
    private boolean isDeletedByReceiver = false;

    public enum SenderReceiverRole {
        CONTROL_ROOM,
        RESCUE_TEAM
    }

    public enum MessageType {
        PREDEFINED,
        CUSTOM
    }

    public Message() {
    }

    public Long getId() {
        return id;
    }

    public SenderReceiverRole getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(SenderReceiverRole senderRole) {
        this.senderRole = senderRole;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public SenderReceiverRole getReceiverRole() {
        return receiverRole;
    }

    public void setReceiverRole(SenderReceiverRole receiverRole) {
        this.receiverRole = receiverRole;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isDeletedBySender() {
        return isDeletedBySender;
    }

    public void setDeletedBySender(boolean deletedBySender) {
        isDeletedBySender = deletedBySender;
    }

    public boolean isDeletedByReceiver() {
        return isDeletedByReceiver;
    }

    public void setDeletedByReceiver(boolean deletedByReceiver) {
        isDeletedByReceiver = deletedByReceiver;
    }
}