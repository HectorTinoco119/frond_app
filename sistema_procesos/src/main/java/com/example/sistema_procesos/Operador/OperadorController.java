package com.example.sistema_procesos.Operador;

import com.example.sistema_procesos.User.User;
import com.example.sistema_procesos.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/operador")
@RequiredArgsConstructor
public class OperadorController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByEmail(username).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("Usuario no encontrado");
        return ResponseEntity.ok(user);
    }
}
