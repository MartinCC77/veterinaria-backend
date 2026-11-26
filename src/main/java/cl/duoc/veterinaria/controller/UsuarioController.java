package cl.duoc.veterinaria.controller;

import cl.duoc.veterinaria.dto.UsuarioActualizarRequest;
import cl.duoc.veterinaria.dto.UsuarioCrearRequest;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.security.UsuarioAutenticado;
import cl.duoc.veterinaria.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "CRUD de usuarios. Exige token JWT")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @Operation(summary = "Crear usuario con rol (solo administrador). 201, 400, 401, 403, 409")
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    @Operation(summary = "Listar todos los usuarios (solo administrador). 200, 401, 403")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Ver un usuario: el administrador cualquiera, los demás solo el propio. 200, 401, 403, 404")
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actor) {
        return service.obtener(id, actor);
    }

    @Operation(summary = "Modificar un usuario (solo el rol exige ser administrador). 200, 400, 401, 403, 404, 409")
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioActualizarRequest datos,
                                      @AuthenticationPrincipal UsuarioAutenticado actor) {
        return service.actualizar(id, datos, actor);
    }

    @Operation(summary = "Eliminar un usuario (solo administrador, no la propia cuenta). 204, 401, 403, 404, 409")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actor) {
        service.eliminar(id, actor);
        return ResponseEntity.noContent().build();
    }
}