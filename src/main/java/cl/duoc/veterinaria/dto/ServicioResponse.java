package cl.duoc.veterinaria.dto;

import cl.duoc.veterinaria.model.Servicio;

// Datos que el servidor devuelve a la app
public record ServicioResponse(
        Long id,
        String codigo,
        String categoria,
        String nombre,
        String especie,
        Integer duracion,
        Integer precio,
        Boolean oferta,
        Integer precioOferta,
        String observaciones,
        String imagen
) {

    public static ServicioResponse desde(Servicio s) {
        return new ServicioResponse(
                s.getId(), s.getCodigo(), s.getCategoria(), s.getNombre(), s.getEspecie(),
                s.getDuracion(), s.getPrecio(), s.getOferta(), s.getPrecioOferta(),
                s.getObservaciones(), s.getImagen());
    }
}