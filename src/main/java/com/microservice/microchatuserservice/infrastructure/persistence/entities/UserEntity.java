package com.microservice.microchatuserservice.infrastructure.persistence.entities;

import com.microservice.microchatuserservice.domain.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private Integer age;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean emailVerified = false;

}
