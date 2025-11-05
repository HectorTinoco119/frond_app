package com.example.sistema_procesos.Auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String country;
    private String role; // ADMIN or OPERADOR
}
