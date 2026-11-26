package cl.duoc.veterinaria.dto;

import cl.duoc.veterinaria.model.Cita;

// Datos que el servidor devuelve a la app
public record CitaResponse(
        Long id,
        Long usuarioId,
        String dueno,
        String correo,
        String mascota,
        String especie,
        Long servicioId,
        String servicioNombre,
        String fecha,
        String hora,
        String motivo,
        Boolean urgencia,
        Integer precioFinal,
        String estado,
        String creadaEn
) {

    public static CitaResponse desde(Cita c) {
        return new CitaResponse(
                c.getId(),
                c.getUsuario().getId(),
                c.getUsuario().getNombre(),
                c.getUsuario().getCorreo(),
                c.getMascota(),
                c.getEspecie(),
                c.getServicio().getId(),
                c.getServicio().getNombre(),
                c.getFecha().toString(),
                c.getHora(),
                c.getMotivo(),
                c.getUrgencia(),
                c.getPrecioFinal(),
                c.getEstado(),
                c.getCreadaEn().toString());
    }
}