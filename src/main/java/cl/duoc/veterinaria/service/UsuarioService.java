package cl.duoc.veterinaria.service;

import cl.duoc.veterinaria.dto.UsuarioActualizarRequest;
import cl.duoc.veterinaria.dto.UsuarioCrearRequest;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.exception.ApiException;
import cl.duoc.veterinaria.model.Usuario;
import cl.duoc.veterinaria.repository.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREAR: el correo debe ser único y la contraseña se guarda cifrada
    public UsuarioResponse crear(UsuarioCrearRequest datos) {
        String correo = datos.correo().trim().toLowerCase();

        if (repository.existsByCorreo(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        usuario.setRol(datos.rol() == null ? "USUARIO" : datos.rol());

        return UsuarioResponse.desde(repository.save(usuario));
    }

    // LEER: todos los usuarios
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    // LEER: un usuario por su id
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.desde(buscar(id));
    }

    // ACTUALIZAR: la contraseña y el rol solo cambian si vienen en la petición
    public UsuarioResponse actualizar(Long id, UsuarioActualizarRequest datos) {
        Usuario usuario = buscar(id);
        String correo = datos.correo().trim().toLowerCase();

        // Si cambia el correo, no puede coincidir con el de otro usuario
        if (!correo.equals(usuario.getCorreo()) && repository.existsByCorreo(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(correo);

        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        }
        if (datos.rol() != null) {
            usuario.setRol(datos.rol());
        }

        return UsuarioResponse.desde(repository.save(usuario));
    }

    // ELIMINAR
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    // Busca un usuario o lanza el error 404
    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}