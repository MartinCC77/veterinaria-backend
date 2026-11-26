package cl.duoc.veterinaria.dto;

// Respuesta del login: el token y los datos públicos del usuario
public record LoginResponse(String token, String tipo, UsuarioResponse usuario) {
}