package com.microservice.microchatuserservice.infrastructure.gateways;

import com.microservice.microchatuserservice.application.gateways.UserGateway;
import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.persistence.mappers.UserMapper;
import com.microservice.microchatuserservice.infrastructure.persistence.UserRepository;
import com.microservice.microchatuserservice.infrastructure.persistence.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryGateway implements UserGateway {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public User register(User user) {
        UserEntity entity = userMapper.domainToEntity(user);
        entity = userRepository.save(entity);
        return userMapper.entityToDomain(entity);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository
                .findUserByEmail(email)
                .map(userMapper::entityToDomain);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository
                .findUserByUsername(username)
                .map(userMapper::entityToDomain);
    }

    @Override
    public Boolean existsUserByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Boolean existsUserByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Page<User> findUsersByUsername(String username, Long userId, Pageable pageable) {
        return userRepository.searchUsers(username, userId, pageable)
                .map(userMapper::entityToDomain);
    }

    @Override
    public Page<User> findUsersById(List<Long> ids, Pageable pageable) {
        return userRepository.findAllByIdIn(ids, pageable)
                .map(userMapper::entityToDomain);
    }
}
