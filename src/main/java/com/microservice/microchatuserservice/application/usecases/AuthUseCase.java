package com.microservice.microchatuserservice.application.usecases;

import com.microservice.microchatuserservice.application.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthUseCase {

    private final UserGateway userGateway;


}
