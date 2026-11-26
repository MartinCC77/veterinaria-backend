package cl.duoc.veterinaria.service;

import cl.duoc.veterinaria.dto.LoginRequest;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.exception.ApiException;
import cl.duoc.veterinaria.model.Usuario;
import cl.duoc.veterinaria.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // Valida el correo y la contraseña. Si algo falla, el mensaje es siempre el mismo
    public UsuarioResponse iniciarSesion(LoginRequest datos) {
        String correo = datos.correo().trim().toLowerCase();

        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(this::credencialesInvalidas);

        // BCrypt compara la clave escrita con la versión cifrada guardada
        if (!passwordEncoder.matches(datos.contrasena(), usuario.getContrasena())) {
            throw credencialesInvalidas();
        }

        return UsuarioResponse.desde(usuario);
    }

    private ApiException credencialesInvalidas() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }
}