package com.microservice.microchatuserservice.application.usecases;

import com.microservice.microchatuserservice.application.exceptions.InvalidCredentialsException;
import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.controller.dto.response.UserPaginatedResponse;
import com.microservice.microchatuserservice.controller.dto.response.UserResponse;
import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.persistence.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserUseCase {

    private final UserGateway userGateway;
    private final UserMapper userMapper;

    public UserPaginatedResponse searchUsersByName(
            String username,
            Long userId,
            int page,
            int size
    ) {
        throwIfTheUserIdIsSmallerThanOne(userId);

        Pageable pageable = PageRequest.of(page, size);

        Page<User> userPage = userGateway.findUsersByUsername(username, userId, pageable);

        List<UserResponse> userList = userPage.getContent().stream()
                .map(userMapper::entityToResponse)
                .toList();

        return UserPaginatedResponse.builder()
                .content(userList)
                .currentPage(userPage.getNumber())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    private void throwIfTheUserIdIsSmallerThanOne(Long userId) {
        if (userId < 1) {
            throw  new InvalidCredentialsException("Please login again");
        }
    }
}
