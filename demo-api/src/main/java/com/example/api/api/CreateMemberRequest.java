package com.example.api.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMemberRequest(
        @Email String email,
        @NotBlank @Size(min = 2, max = 50) String name,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
