package cl.duoc.veterinaria.config;

import cl.duoc.veterinaria.model.Usuario;
import cl.duoc.veterinaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Se ejecuta al arrancar: si la tabla está vacía, crea los usuarios de demostración
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        crear("Administrador San Marcos", "admin@sanmarcos.cl", "admin123", "administrador");
        crear("Recepcionista San Marcos", "recepcion@sanmarcos.cl", "recep123", "recepcionista");
        crear("Juan Pérez", "juan@correo.cl", "dueno123", "dueno");
    }

    private void crear(String nombre, String correo, String contrasena, String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setRol(rol);
        repository.save(usuario);
    }
}