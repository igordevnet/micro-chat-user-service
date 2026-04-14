package com.microservice.microchatuserservice.application.gateways;

import java.util.Optional;

public interface VerifyEmailGateway {
    void createVerifyEmailCode(String email, String code);
    Optional<String> getEmailByCode(String code);
    Optional<String> getCodeByEmail(String email);
    void deleteVerifyEmailCode(String code);
    void updateVerifyEmailCode(String email, String code);
    boolean validateEmailCode(String code);
}
