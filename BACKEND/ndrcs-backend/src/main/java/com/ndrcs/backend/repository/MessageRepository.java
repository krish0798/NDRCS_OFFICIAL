package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByReceiverRoleAndReceiverIdAndIsDeletedByReceiverFalseOrderByCreatedAtDesc(
            Message.SenderReceiverRole receiverRole,
            Long receiverId
    );

    List<Message> findBySenderRoleAndSenderIdOrderByCreatedAtDesc(
            Message.SenderReceiverRole senderRole,
            Long senderId
    );
}