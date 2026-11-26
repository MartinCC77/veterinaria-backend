package cl.duoc.veterinaria.repository;

import cl.duoc.veterinaria.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca un usuario por su correo (se usa para el login)
    Optional<Usuario> findByCorreo(String correo);

    // Comprueba si ya existe un usuario con ese correo
    boolean existsByCorreo(String correo);
}