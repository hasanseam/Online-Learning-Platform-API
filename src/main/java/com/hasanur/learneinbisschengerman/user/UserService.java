package com.hasanur.learneinbisschengerman.user;

import com.hasanur.learneinbisschengerman.exceptions.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public UserResponseDto register(UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email Already Registered");
        }

        User user = User.builder()
                .fullName(dto.fullName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        return new UserResponseDto(user.getId(), user.getFullName(), user.getEmail());
    }

    // READ ONE
    public UserResponseDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponseDto(user.getId(), user.getFullName(), user.getEmail());
    }

    // READ ALL
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // UPDATE

    public UserResponseDto updateUser(Long id, UserRegisterDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        user.setFullName(dto.fullName());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);

        return mapToDto(user);
    }

    // DELETE
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(user.getId(), user.getFullName(), user.getEmail());
    }
}
