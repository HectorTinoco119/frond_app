package com.example.sistema_procesos.Auth;

import com.example.sistema_procesos.Jwt.JwtService;
import com.example.sistema_procesos.Token.TokenService;
import com.example.sistema_procesos.User.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        String token = jwtService.generateTokenWithJti(user);
        tokenService.registerToken(user.getEmail(), token);
        return AuthResponse.builder().token(token).build();
    }

    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> { throw new RuntimeException("El correo ya está registrado"); });

        String r = request.getRole() == null ? "OPERADOR" : request.getRole().toUpperCase();
        if (!r.equals("ADMIN") && !r.equals("OPERADOR")) throw new RuntimeException("Rol inválido. Use ADMIN u OPERADOR");

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .country(request.getCountry())
                .role(Role.valueOf("ROLE_" + r))
                .build();

        userRepository.save(user);

        String token = jwtService.generateTokenWithJti(user);
        tokenService.registerToken(user.getEmail(), token);
        return AuthResponse.builder().token(token).build();
    }

    public User getUserFromToken(String bearerToken) {
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        String email = jwtService.getUsernameFromToken(token);
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public void logout(String bearerToken) {
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : bearerToken;
        tokenService.invalidateToken(token);
    }
}
