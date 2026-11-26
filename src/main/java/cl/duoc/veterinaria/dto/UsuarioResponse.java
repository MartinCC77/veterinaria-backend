package cl.duoc.veterinaria.dto;

import cl.duoc.veterinaria.model.Usuario;

// Datos que el servidor devuelve a la app. Nunca incluye la contraseña
public record UsuarioResponse(Long id, String nombre, String correo, String rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol()
        );
    }
}