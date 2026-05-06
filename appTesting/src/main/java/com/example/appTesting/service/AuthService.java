package com.example.appTesting.service;

import org.springframework.aot.hint.annotation.RegisterReflection;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.appTesting.dto.AuthResponseDTO;
import com.example.appTesting.dto.LoginRequestDTO;
import com.example.appTesting.dto.RegisterRequestDTO;
import com.example.appTesting.model.User;
import com.example.appTesting.model.enums.RoleName;
import com.example.appTesting.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;

    public AuthResponseDTO register(RegisterRequestDTO request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }
        RoleName rol;

        try {
            rol = request.getRol();
        } catch (Exception e) {
            rol = RoleName.ROLE_USER;
        }
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(rol)
            .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return AuthResponseDTO.builder()
            .token(token)
            .build();
    }

    public AuthResponseDTO login(LoginRequestDTO request){
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("Email no encontrado"));
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new BadCredentialsException("Contraseña incorrecta");
        }
        String token = jwtService.generateToken(user);
        return AuthResponseDTO.builder()
            .token(token)
            .build();

    }
}
