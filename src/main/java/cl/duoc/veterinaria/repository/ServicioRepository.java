package cl.duoc.veterinaria.repository;

import cl.duoc.veterinaria.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    // Comprueba si ya existe un servicio con ese código
    boolean existsByCodigo(String codigo);
}