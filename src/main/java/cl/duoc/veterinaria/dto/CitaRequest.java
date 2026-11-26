package cl.duoc.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Datos que llegan desde la app para solicitar una cita.
// No incluye dueño ni precio: los pone el servidor.
public record CitaRequest(

        @NotBlank(message = "El nombre de la mascota es requerido")
        @Size(min = 2, max = 100, message = "El nombre de la mascota debe tener entre 2 y 100 caracteres")
        String mascota,

        @NotBlank(message = "La especie es requerida")
        String especie,

        @NotNull(message = "El servicio es requerido")
        Long servicioId,

        @NotBlank(message = "La fecha es requerida")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha debe tener el formato AAAA-MM-DD")
        String fecha,

        @NotBlank(message = "La hora es requerida")
        String hora,

        @NotBlank(message = "El motivo es requerido")
        @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
        String motivo,

        // Si no viene, se entiende que no es urgencia
        Boolean urgencia
) {
}