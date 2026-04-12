package com.microservice.microchatuserservice.infrastructure.mappers;

import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.persistence.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {
    UserEntity entityToDomain(User user);
    User domainToEntity(UserEntity userEntity);
}
