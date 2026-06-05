package com.library.controller;

import com.library.dto.AuthResponse;
import com.library.dto.LoginRequest;
import com.library.model.User;
import com.library.model.UserStatus;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request)
    {
        if (request == null || request.getEmail() == null || request.getPassword() == null)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email și parolă obligatorii");
        }

        User user = userRepository.findByEmail(request.getEmail().trim()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email sau parolă invalidă");
        }

        if (user.getStatus() != UserStatus.ACTIVE)
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Cont " + user.getStatus().toString().toLowerCase());
        }

        String token = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse(
                token,
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus(),
                user.getRole().getRoleId(),
                user.getRole().getRoleName().toString()
        );

        return ResponseEntity.ok(response);
    }
}