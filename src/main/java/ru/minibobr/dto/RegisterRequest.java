package ru.minibobr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "{validation.username.required}")
    private String username;

    @NotBlank(message = "{validation.password.required}")
    private String password;
}


