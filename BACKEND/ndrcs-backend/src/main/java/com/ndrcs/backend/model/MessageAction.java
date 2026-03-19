package com.ndrcs.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_actions")
public class MessageAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "acted_by_role", nullable = false)
    private Message.SenderReceiverRole actedByRole;

    @Column(name = "acted_by_id", nullable = false)
    private Long actedById;

    @Column(name = "acted_at", nullable = false)
    private LocalDateTime actedAt = LocalDateTime.now();

    public enum ActionType {
        ACKNOWLEDGED,
        CLEARED,
        DELETED
    }

    public MessageAction() {
    }

    public Long getId() {
        return id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public Message.SenderReceiverRole getActedByRole() {
        return actedByRole;
    }

    public void setActedByRole(Message.SenderReceiverRole actedByRole) {
        this.actedByRole = actedByRole;
    }

    public Long getActedById() {
        return actedById;
    }

    public void setActedById(Long actedById) {
        this.actedById = actedById;
    }

    public LocalDateTime getActedAt() {
        return actedAt;
    }
}