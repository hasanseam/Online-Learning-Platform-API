package com.hasanur.learneinbisschengerman.auth;

import com.hasanur.learneinbisschengerman.auth.Dtos.LoginRequest;
import com.hasanur.learneinbisschengerman.auth.Dtos.LoginResponse;
import com.hasanur.learneinbisschengerman.auth.Dtos.RefreshRequest;
import com.hasanur.learneinbisschengerman.auth.Dtos.RefreshResponse;
import com.hasanur.learneinbisschengerman.auth.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refreshToken(request);
    }
}
