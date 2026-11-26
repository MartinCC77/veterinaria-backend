package cl.duoc.veterinaria.controller;

import cl.duoc.veterinaria.dto.ServicioRequest;
import cl.duoc.veterinaria.dto.ServicioResponse;
import cl.duoc.veterinaria.service.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/servicios")
@Tag(name = "Servicios", description = "Catálogo de servicios. Ver es público; crear, modificar y eliminar exige rol administrador")
public class ServicioController {

    private final ServicioService service;

    public ServicioController(ServicioService service) {
        this.service = service;
    }

    @Operation(summary = "Listar servicios (público). 200")
    @GetMapping
    public List<ServicioResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Ver un servicio (público). 200, 404")
    @GetMapping("/{id}")
    public ServicioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @Operation(summary = "Crear servicio (solo administrador). 201, 400, 401, 403, 409")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @Operation(summary = "Modificar servicio (solo administrador). 200, 400, 401, 403, 404, 409")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest datos) {
        return service.actualizar(id, datos);
    }

    @Operation(summary = "Eliminar servicio (solo administrador). 204, 401, 403, 404")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}