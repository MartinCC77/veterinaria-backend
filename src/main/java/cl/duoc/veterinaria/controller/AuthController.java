package cl.duoc.veterinaria.controller;

import cl.duoc.veterinaria.dto.LoginRequest;
import cl.duoc.veterinaria.dto.LoginResponse;
import cl.duoc.veterinaria.dto.RegistroRequest;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.service.AuthService;
import cl.duoc.veterinaria.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión y registro (públicos)")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Iniciar sesión: devuelve un token JWT (200) o 401 si las credenciales son incorrectas")
    @PostMapping("/login")
    public LoginResponse iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return authService.iniciarSesion(datos);
    }

    @Operation(summary = "Registrar un dueño de mascota (201). El rol lo asigna el servidor. 409 si el correo existe")
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(datos));
    }
}