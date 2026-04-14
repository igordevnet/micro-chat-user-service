package com.microservice.microchatuserservice.application.gateways;

public interface EmailGateway {
    void sendVerificationEmail(String to, String code);

    void sendResetPasswordEmail(String to, String token);
}
