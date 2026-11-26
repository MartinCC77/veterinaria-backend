package cl.duoc.veterinaria.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Datos que llegan desde la app para crear o modificar un servicio
public record ServicioRequest(

        @NotBlank(message = "El código es requerido")
        @Size(max = 20, message = "El código no puede superar los 20 caracteres")
        String codigo,

        @NotBlank(message = "La categoría es requerida")
        @Size(max = 50, message = "La categoría no puede superar los 50 caracteres")
        String categoria,

        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "La especie es requerida")
        @Size(max = 50, message = "La especie no puede superar los 50 caracteres")
        String especie,

        @NotNull(message = "La duración es requerida")
        @Min(value = 5, message = "La duración mínima es de 5 minutos")
        @Max(value = 480, message = "La duración máxima es de 480 minutos")
        Integer duracion,

        @NotNull(message = "El precio es requerido")
        @Min(value = 0, message = "El precio no puede ser negativo")
        @Max(value = 100000000, message = "El precio es demasiado alto")
        Integer precio,

        // Si no viene, se entiende que no está en oferta
        Boolean oferta,

        @Min(value = 0, message = "El precio de oferta no puede ser negativo")
        Integer precioOferta,

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
        String observaciones,

        @Size(max = 200, message = "La ruta de la imagen no puede superar los 200 caracteres")
        String imagen
) {
}