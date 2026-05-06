package com.hasanur.learneinbisschengerman.user;

import com.hasanur.learneinbisschengerman.user.UserRegisterDto;
import com.hasanur.learneinbisschengerman.user.UserResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("Should successfully register a new user")
    void shouldRegisterUser() {
        // Given
        UserRegisterDto registerDto = new UserRegisterDto(
                "John Doe",
                "john.doe@example.com",
                "password123"
        );

        UserResponseDto responseDto = new UserResponseDto(
                1L,
                "John Doe",
                "john.doe@example.com"
        );

        when(userService.register(any(UserRegisterDto.class))).thenReturn(responseDto);

        // When
        ResponseEntity<UserResponseDto> response = userController.register(registerDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        assertEquals("John Doe", response.getBody().fullName());
        assertEquals("john.doe@example.com", response.getBody().email());
    }

    @Test
    @DisplayName("Should fail validation when registration data is invalid")
    void shouldFailValidationWhenInvalidData() {
        // Given
        UserRegisterDto invalidDto = new UserRegisterDto(
                "", // Blank name
                "invalid-email", // Invalid email format
                "123" // Too short password
        );

        // When
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserRegisterDto>> violations = validator.validate(invalidDto);

        // Then
        assertFalse(violations.isEmpty(), "Validation should fail for invalid data");
        assertEquals(3, violations.size(), "Should have exactly 3 validation errors");
    }
}
