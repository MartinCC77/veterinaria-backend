package cl.duoc.veterinaria.service;

import cl.duoc.veterinaria.dto.ServicioRequest;
import cl.duoc.veterinaria.dto.ServicioResponse;
import cl.duoc.veterinaria.exception.ApiException;
import cl.duoc.veterinaria.model.Servicio;
import cl.duoc.veterinaria.repository.ServicioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ServicioService {

    private final ServicioRepository repository;

    public ServicioService(ServicioRepository repository) {
        this.repository = repository;
    }

    // LEER todos (público)
    public List<ServicioResponse> listar() {
        return repository.findAll().stream().map(ServicioResponse::desde).toList();
    }

    // LEER uno (público)
    public ServicioResponse obtener(Long id) {
        return ServicioResponse.desde(buscar(id));
    }

    // CREAR: el código debe ser único y la oferta debe ser coherente con el precio
    public ServicioResponse crear(ServicioRequest datos) {
        String codigo = datos.codigo().trim().toUpperCase();
        if (repository.existsByCodigo(codigo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un servicio con ese código");
        }
        validarOferta(datos);

        Servicio servicio = new Servicio();
        copiarDatos(servicio, datos, codigo);
        return ServicioResponse.desde(repository.save(servicio));
    }

    // ACTUALIZAR: si cambia el código, no puede coincidir con el de otro servicio
    public ServicioResponse actualizar(Long id, ServicioRequest datos) {
        Servicio servicio = buscar(id);
        String codigo = datos.codigo().trim().toUpperCase();
        if (!codigo.equals(servicio.getCodigo()) && repository.existsByCodigo(codigo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un servicio con ese código");
        }
        validarOferta(datos);

        copiarDatos(servicio, datos, codigo);
        return ServicioResponse.desde(repository.save(servicio));
    }

    // ELIMINAR
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    // ---- métodos auxiliares ----

    // Regla de negocio: si está en oferta, el precio de oferta es obligatorio y menor al normal
    private void validarOferta(ServicioRequest datos) {
        boolean enOferta = Boolean.TRUE.equals(datos.oferta());
        if (enOferta) {
            if (datos.precioOferta() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "El precio de oferta es requerido cuando el servicio está en oferta");
            }
            if (datos.precioOferta() >= datos.precio()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "El precio de oferta debe ser menor al precio normal");
            }
        }
    }

    private void copiarDatos(Servicio servicio, ServicioRequest datos, String codigo) {
        boolean enOferta = Boolean.TRUE.equals(datos.oferta());
        servicio.setCodigo(codigo);
        servicio.setCategoria(datos.categoria().trim());
        servicio.setNombre(datos.nombre().trim());
        servicio.setEspecie(datos.especie().trim());
        servicio.setDuracion(datos.duracion());
        servicio.setPrecio(datos.precio());
        servicio.setOferta(enOferta);
        // Si no está en oferta, se descarta el precio de oferta
        servicio.setPrecioOferta(enOferta ? datos.precioOferta() : null);
        servicio.setObservaciones(datos.observaciones());
        servicio.setImagen(datos.imagen());
    }

    private Servicio buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
    }
}