package cl.duoc.veterinaria.security;

// Datos del usuario que vienen dentro del token JWT.
// Es lo que el servidor sabe de "quién está llamando" en cada petición protegida.
public record UsuarioAutenticado(Long id, String correo, String rol) {
}