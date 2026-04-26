package com.hasanur.learneinbisschengerman.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDto(

                @NotBlank(message = "Full name is required") String fullName,

                @Email(message = "Invalid email") @NotBlank(message = "Email is required") String email,

                @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 8 characters") String password) {
}
