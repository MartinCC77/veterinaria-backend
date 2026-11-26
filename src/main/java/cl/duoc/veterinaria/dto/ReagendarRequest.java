package cl.duoc.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Nuevo horario que propone el personal para una cita
public record ReagendarRequest(

        @NotBlank(message = "La fecha es requerida")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha debe tener el formato AAAA-MM-DD")
        String fecha,

        @NotBlank(message = "La hora es requerida")
        String hora
) {
}