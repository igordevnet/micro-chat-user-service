package com.microservice.microchatuserservice.infrastructure.gateways;

import com.microservice.microchatuserservice.application.gateways.VerifyEmailGateway;
import com.microservice.microchatuserservice.infrastructure.persistence.VerifyEmailRepository;
import com.microservice.microchatuserservice.infrastructure.persistence.entities.EmailCodeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VerifyEmailRepositoryGateway implements VerifyEmailGateway {

    private final VerifyEmailRepository codeRepository;

    @Override
    public void createVerifyEmailCode(String email, String code) {
        EmailCodeEntity myCode = EmailCodeEntity.builder()
                .code(code)
                .email(email)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
        codeRepository.save(myCode);
    }

    @Override
    public Optional<String> getEmailByCode(String code) {
        return codeRepository.findByCode(code)
                .map(EmailCodeEntity::getEmail)
                .map(String::valueOf);
    }

    @Override
    public Optional<String> getCodeByEmail(String email) {
        return codeRepository.findByEmail(email)
                .map(EmailCodeEntity::getCode)
                .map(String::valueOf);
    }

    @Override
    public void deleteVerifyEmailCode(String code) {
        codeRepository.findByCode(code).ifPresent(codeRepository::delete);
    }

    @Override
    public void updateVerifyEmailCode(String email, String code) {
        codeRepository.updateCodeByEmail(email, code);
    }

    @Override
    public boolean validateEmailCode(String code) {
        return codeRepository.findByCode(code)
                .map(t -> !t.getExpiryDate().isBefore(LocalDateTime.now()))
                .orElse(false);
    }
}
