package cl.duoc.veterinaria.controller;

import cl.duoc.veterinaria.dto.CitaRequest;
import cl.duoc.veterinaria.dto.CitaResponse;
import cl.duoc.veterinaria.dto.ReagendarRequest;
import cl.duoc.veterinaria.security.UsuarioAutenticado;
import cl.duoc.veterinaria.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citas")
@Tag(name = "Citas", description = "Solicitudes de cita. Cada dueño ve solo las suyas; el personal ve todas")
@SecurityRequirement(name = "bearerAuth")
public class CitaController {

    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    @Operation(summary = "Solicitar una cita (queda pendiente, a nombre del usuario del token). 201, 400, 401")
    @PostMapping
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaRequest datos,
                                              @AuthenticationPrincipal UsuarioAutenticado actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos, actor));
    }

    @Operation(summary = "Listar citas: el personal todas, el dueño solo las suyas. Filtro opcional por estado. 200, 400, 401")
    @GetMapping
    public List<CitaResponse> listar(@RequestParam(required = false) String estado,
                                     @AuthenticationPrincipal UsuarioAutenticado actor) {
        return service.listar(actor, estado);
    }

    @Operation(summary = "Ver el detalle de una cita (solo el dueño o el personal). 200, 401, 403, 404")
    @GetMapping("/{id}")
    public CitaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actor) {
        return service.obtener(id, actor);
    }

    @Operation(summary = "Confirmar una cita (solo personal). 409 si el horario ya está ocupado. 200, 401, 403, 404, 409")
    @PatchMapping("/{id}/confirmar")
    public CitaResponse confirmar(@PathVariable Long id) {
        return service.confirmar(id);
    }

    @Operation(summary = "Proponer otro horario (solo personal). 409 si está ocupado. 200, 400, 401, 403, 404, 409")
    @PatchMapping("/{id}/reagendar")
    public CitaResponse reagendar(@PathVariable Long id, @Valid @RequestBody ReagendarRequest datos) {
        return service.reagendar(id, datos);
    }

    @Operation(summary = "Cancelar una cita (el dueño o el personal). 200, 401, 403, 404, 409")
    @PatchMapping("/{id}/cancelar")
    public CitaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actor) {
        return service.cancelar(id, actor);
    }
}