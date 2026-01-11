package com.ecommerce.service;

import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.AuthResponse;
import com.ecommerce.dto.response.UserResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final DemoDataService demoDataService;
    private final DemoModeService demoModeService;

    private static final String DEMO_EMAIL_DOMAIN = "@ecommerce.local";
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .roles(Set.of(User.Role.USER))
                .active(true)
                .build();
        
        user = userRepository.save(user);
        
        String token = jwtTokenProvider.generateToken(user.getEmail());
        return AuthResponse.of(token, UserResponse.fromUser(user));
    }
    
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));
        
        String token = jwtTokenProvider.generateToken(authentication);
        return AuthResponse.of(token, UserResponse.fromUser(user));
    }

    public AuthResponse demoLogin() {
        demoDataService.ensureDemoData(null);

        String demoEmail = "demo+" + UUID.randomUUID() + DEMO_EMAIL_DOMAIN;
        User user = demoModeService.getOrCreateDemoUserByEmail(demoEmail);
        user.setCreatedAt(LocalDateTime.now());
        user.setRoles(Set.of(User.Role.USER));
        user.setActive(true);

        String token = jwtTokenProvider.generateToken(demoEmail);
        return AuthResponse.of(token, UserResponse.fromUser(user));
    }
}
