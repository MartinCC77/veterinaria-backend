package cl.duoc.veterinaria.service;

import cl.duoc.veterinaria.dto.LoginRequest;
import cl.duoc.veterinaria.dto.LoginResponse;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.exception.ApiException;
import cl.duoc.veterinaria.model.Usuario;
import cl.duoc.veterinaria.repository.UsuarioRepository;
import cl.duoc.veterinaria.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Valida correo y contraseña. Si algo falla, el mensaje es siempre el mismo
    public LoginResponse iniciarSesion(LoginRequest datos) {
        String correo = datos.correo().trim().toLowerCase();

        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(this::credencialesInvalidas);

        // BCrypt compara la clave escrita con la versión cifrada guardada
        if (!passwordEncoder.matches(datos.contrasena(), usuario.getContrasena())) {
            throw credencialesInvalidas();
        }

        String token = jwtService.generarToken(usuario.getId(), usuario.getCorreo(), usuario.getRol());
        return new LoginResponse(token, "Bearer", UsuarioResponse.desde(usuario));
    }

    private ApiException credencialesInvalidas() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }
}