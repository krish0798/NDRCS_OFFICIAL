package com.ndrcs.backend.service;

import com.ndrcs.backend.dto.MessageActionRequest;
import com.ndrcs.backend.dto.SendMessageRequest;
import com.ndrcs.backend.model.AuthUser;
import com.ndrcs.backend.model.ControlRoomProfile;
import com.ndrcs.backend.model.Incident;
import com.ndrcs.backend.model.Message;
import com.ndrcs.backend.model.MessageAction;
import com.ndrcs.backend.model.RescueTeamProfile;
import com.ndrcs.backend.repository.AuthUserRepository;
import com.ndrcs.backend.repository.ControlRoomProfileRepository;
import com.ndrcs.backend.repository.IncidentRepository;
import com.ndrcs.backend.repository.MessageActionRepository;
import com.ndrcs.backend.repository.MessageRepository;
import com.ndrcs.backend.repository.RescueTeamProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final AuthUserRepository authUserRepository;
    private final IncidentRepository incidentRepository;
    private final MessageRepository messageRepository;
    private final MessageActionRepository messageActionRepository;
    private final ControlRoomProfileRepository controlRoomProfileRepository;
    private final RescueTeamProfileRepository rescueTeamProfileRepository;

    public MessageService(AuthUserRepository authUserRepository,
                          IncidentRepository incidentRepository,
                          MessageRepository messageRepository,
                          MessageActionRepository messageActionRepository,
                          ControlRoomProfileRepository controlRoomProfileRepository,
                          RescueTeamProfileRepository rescueTeamProfileRepository) {
        this.authUserRepository = authUserRepository;
        this.incidentRepository = incidentRepository;
        this.messageRepository = messageRepository;
        this.messageActionRepository = messageActionRepository;
        this.controlRoomProfileRepository = controlRoomProfileRepository;
        this.rescueTeamProfileRepository = rescueTeamProfileRepository;
    }

    public String sendMessage(SendMessageRequest request) {

        AuthUser senderAuth = authUserRepository.findByUserId(request.getSenderUserId()).orElse(null);

        if (senderAuth == null) {
            return "Invalid sender user ID";
        }

        Message.SenderReceiverRole senderRole;
        Long senderProfileId;

        if (senderAuth.getRole() == AuthUser.Role.CONTROL_ROOM) {
            senderRole = Message.SenderReceiverRole.CONTROL_ROOM;
            senderProfileId = controlRoomProfileRepository.findAll().stream()
                    .filter(c -> c.getAuthUser().getId().equals(senderAuth.getId()))
                    .findFirst()
                    .map(ControlRoomProfile::getId)
                    .orElse(null);
        } else if (senderAuth.getRole() == AuthUser.Role.RESCUE_TEAM) {
            senderRole = Message.SenderReceiverRole.RESCUE_TEAM;
            senderProfileId = rescueTeamProfileRepository.findAll().stream()
                    .filter(r -> r.getAuthUser().getId().equals(senderAuth.getId()))
                    .findFirst()
                    .map(RescueTeamProfile::getId)
                    .orElse(null);
        } else {
            return "Only control room and rescue team can send messages";
        }

        if (senderProfileId == null) {
            return "Sender profile not found";
        }

        Message.SenderReceiverRole receiverRole;
        try {
            receiverRole = Message.SenderReceiverRole.valueOf(request.getReceiverRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid receiver role";
        }

        Message message = new Message();
        message.setSenderRole(senderRole);
        message.setSenderId(senderProfileId);
        message.setReceiverRole(receiverRole);
        message.setReceiverId(request.getReceiverId());
        message.setMessageText(request.getMessageText());

        if (request.getMessageType() != null) {
            try {
                message.setMessageType(Message.MessageType.valueOf(request.getMessageType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                message.setMessageType(Message.MessageType.CUSTOM);
            }
        }

        if (request.getIncidentId() != null) {
            Incident incident = incidentRepository.findById(request.getIncidentId()).orElse(null);
            message.setIncident(incident);
        }

        messageRepository.save(message);
        return "Message sent successfully";
    }

    public List<Message> getInbox(String role, Long profileId) {
        Message.SenderReceiverRole receiverRole = Message.SenderReceiverRole.valueOf(role.toUpperCase());
        return messageRepository.findByReceiverRoleAndReceiverIdAndIsDeletedByReceiverFalseOrderByCreatedAtDesc(receiverRole, profileId);
    }

    public String performMessageAction(MessageActionRequest request) {

        AuthUser authUser = authUserRepository.findByUserId(request.getUserId()).orElse(null);

        if (authUser == null) {
            return "Invalid user ID";
        }

        Message message = messageRepository.findById(request.getMessageId()).orElse(null);
        if (message == null) {
            return "Message not found";
        }

        MessageAction.ActionType actionType;
        try {
            actionType = MessageAction.ActionType.valueOf(request.getActionType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid action type";
        }

        Message.SenderReceiverRole actingRole;
        Long actingProfileId;

        if (authUser.getRole() == AuthUser.Role.CONTROL_ROOM) {
            actingRole = Message.SenderReceiverRole.CONTROL_ROOM;
            actingProfileId = controlRoomProfileRepository.findAll().stream()
                    .filter(c -> c.getAuthUser().getId().equals(authUser.getId()))
                    .findFirst()
                    .map(ControlRoomProfile::getId)
                    .orElse(null);
        } else if (authUser.getRole() == AuthUser.Role.RESCUE_TEAM) {
            actingRole = Message.SenderReceiverRole.RESCUE_TEAM;
            actingProfileId = rescueTeamProfileRepository.findAll().stream()
                    .filter(r -> r.getAuthUser().getId().equals(authUser.getId()))
                    .findFirst()
                    .map(RescueTeamProfile::getId)
                    .orElse(null);
        } else {
            return "Only control room or rescue team can perform message actions";
        }

        if (actingProfileId == null) {
            return "Acting profile not found";
        }

        if (actionType == MessageAction.ActionType.DELETED) {
            if (message.getSenderRole() == actingRole && message.getSenderId().equals(actingProfileId)) {
                message.setDeletedBySender(true);
            }
            if (message.getReceiverRole() == actingRole && message.getReceiverId().equals(actingProfileId)) {
                message.setDeletedByReceiver(true);
            }
            messageRepository.save(message);
        }

        MessageAction action = new MessageAction();
        action.setMessage(message);
        action.setActionType(actionType);
        action.setActedByRole(actingRole);
        action.setActedById(actingProfileId);

        messageActionRepository.save(action);

        return "Message action recorded successfully";
    }
}