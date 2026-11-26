package cl.duoc.veterinaria.repository;

import cl.duoc.veterinaria.model.Cita;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Todas las citas, ordenadas por fecha y hora (para el personal)
    List<Cita> findAllByOrderByFechaAscHoraAsc();

    // Solo las citas de un usuario (para el dueño de mascota)
    List<Cita> findByUsuarioIdOrderByFechaAscHoraAsc(Long usuarioId);

    // Cuenta las citas con ese estado en ese horario, sin contar la cita indicada.
    // Sirve para detectar el conflicto de horario.
    long countByFechaAndHoraAndEstadoAndIdNot(LocalDate fecha, String hora, String estado, Long id);
}