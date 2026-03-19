package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.MessageAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageActionRepository extends JpaRepository<MessageAction, Long> {
}