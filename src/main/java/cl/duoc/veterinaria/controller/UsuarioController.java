package cl.duoc.veterinaria.controller;

import cl.duoc.veterinaria.dto.UsuarioActualizarRequest;
import cl.duoc.veterinaria.dto.UsuarioCrearRequest;
import cl.duoc.veterinaria.dto.UsuarioResponse;
import cl.duoc.veterinaria.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // POST /api/usuarios -> crea un usuario (201)
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(datos));
    }

    // GET /api/usuarios -> lista todos
    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    // GET /api/usuarios/{id} -> un usuario
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    // PUT /api/usuarios/{id} -> modifica un usuario
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioActualizarRequest datos) {
        return service.actualizar(id, datos);
    }

    // DELETE /api/usuarios/{id} -> elimina un usuario (204)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}