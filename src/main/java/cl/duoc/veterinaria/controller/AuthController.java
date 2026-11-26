package cl.duoc.veterinaria.controller;

import cl.duoc.veterinaria.dto.LoginRequest;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // POST /api/auth/login -> devuelve los datos del usuario si las credenciales son correctas
    @PostMapping("/login")
    public UsuarioResponse iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return service.iniciarSesion(datos);
    }
}