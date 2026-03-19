package com.ndrcs.backend.repository;

import com.ndrcs.backend.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    List<EmailVerification> findByEmail(String email);

    Optional<EmailVerification> findTopByEmailAndOtpCodeOrderByCreatedAtDesc(String email, String otpCode);
}