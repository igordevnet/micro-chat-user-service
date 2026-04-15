package com.microservice.microchatuserservice.infrastructure.persistence.mappers;

import com.microservice.microchatuserservice.domain.User;
import com.microservice.microchatuserservice.infrastructure.persistence.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity domainToEntity(User user);
    User entityToDomain(UserEntity userEntity);
}
