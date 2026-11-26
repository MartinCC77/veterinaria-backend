package cl.duoc.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;

// Datos que llegan desde la app para iniciar sesión
public record LoginRequest(

        @NotBlank(message = "El correo es requerido")
        String correo,

        @NotBlank(message = "La contraseña es requerida")
        String contrasena
) {
}