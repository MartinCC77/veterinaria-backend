package cl.duoc.veterinaria.service;

import cl.duoc.veterinaria.dto.RegistroRequest;
import cl.duoc.veterinaria.dto.UsuarioActualizarRequest;
import cl.duoc.veterinaria.dto.UsuarioCrearRequest;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.exception.ApiException;
import cl.duoc.veterinaria.model.Usuario;
import cl.duoc.veterinaria.repository.UsuarioRepository;
import cl.duoc.veterinaria.security.UsuarioAutenticado;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private static final String ROL_ADMIN = "administrador";
    private static final String ROL_DUENO = "dueno";

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTRO PÚBLICO: el rol lo decide el servidor, siempre "dueno"
    public UsuarioResponse registrar(RegistroRequest datos) {
        return guardarNuevo(datos.nombre(), datos.correo(), datos.contrasena(), ROL_DUENO);
    }

    // CREAR (solo administrador, lo exige SecurityConfig): puede elegir el rol
    public UsuarioResponse crear(UsuarioCrearRequest datos) {
        String rol = datos.rol() == null ? ROL_DUENO : datos.rol();
        return guardarNuevo(datos.nombre(), datos.correo(), datos.contrasena(), rol);
    }

    // LEER todos (solo administrador)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    // LEER uno: el administrador ve cualquiera; los demás solo su propia cuenta
    public UsuarioResponse obtener(Long id, UsuarioAutenticado actor) {
        verificarAcceso(id, actor);
        return UsuarioResponse.desde(buscar(id));
    }

    // ACTUALIZAR: cada uno modifica su cuenta; solo el administrador cambia roles
    public UsuarioResponse actualizar(Long id, UsuarioActualizarRequest datos, UsuarioAutenticado actor) {
        verificarAcceso(id, actor);
        Usuario usuario = buscar(id);
        String correo = datos.correo().trim().toLowerCase();

        if (!correo.equals(usuario.getCorreo()) && repository.existsByCorreo(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }
        if (datos.rol() != null && !datos.rol().equals(usuario.getRol())) {
            if (!esAdmin(actor)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Solo el administrador puede cambiar el rol");
            }
            usuario.setRol(datos.rol());
        }

        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(correo);
        if (datos.contrasena() != null && !datos.contrasena().isBlank()) {
            usuario.setContrasena(passwordEncoder.encode(datos.contrasena()));
        }
        return UsuarioResponse.desde(repository.save(usuario));
    }

    // ELIMINAR (solo administrador): no puede eliminar su propia cuenta
    public void eliminar(Long id, UsuarioAutenticado actor) {
        if (actor.id().equals(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta");
        }
        repository.delete(buscar(id));
    }

    // ---- métodos auxiliares ----

    private UsuarioResponse guardarNuevo(String nombre, String correoOriginal, String contrasena, String rol) {
        String correo = correoOriginal.trim().toLowerCase();
        if (repository.existsByCorreo(correo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setRol(rol);
        return UsuarioResponse.desde(repository.save(usuario));
    }

    // Cambiar el id en la URL no sirve: si no eres admin, solo puedes tocar tu propio id
    private void verificarAcceso(Long id, UsuarioAutenticado actor) {
        if (!esAdmin(actor) && !actor.id().equals(id)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes acceder a la cuenta de otra persona");
        }
    }

    private boolean esAdmin(UsuarioAutenticado actor) {
        return ROL_ADMIN.equals(actor.rol());
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}