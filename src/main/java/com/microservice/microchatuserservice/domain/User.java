package com.microservice.microchatuserservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private Integer age;
    private boolean emailVerified;
    private Role role;

    public void verifyEmail() {
        this.emailVerified = true;
    }
}
