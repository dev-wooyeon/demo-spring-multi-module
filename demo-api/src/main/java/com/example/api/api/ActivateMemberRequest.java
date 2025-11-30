package com.example.api.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ActivateMemberRequest(
        @Email String email,
        @NotBlank String code
) {
}
