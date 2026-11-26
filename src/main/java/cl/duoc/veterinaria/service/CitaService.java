package cl.duoc.veterinaria.service;

import cl.duoc.veterinaria.dto.CitaRequest;
import cl.duoc.veterinaria.dto.CitaResponse;
import cl.duoc.veterinaria.dto.ReagendarRequest;
import cl.duoc.veterinaria.exception.ApiException;
import cl.duoc.veterinaria.model.Cita;
import cl.duoc.veterinaria.model.Servicio;
import cl.duoc.veterinaria.model.Usuario;
import cl.duoc.veterinaria.repository.CitaRepository;
import cl.duoc.veterinaria.repository.ServicioRepository;
import cl.duoc.veterinaria.repository.UsuarioRepository;
import cl.duoc.veterinaria.security.UsuarioAutenticado;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CitaService {

    private static final String PENDIENTE = "pendiente";
    private static final String CONFIRMADA = "confirmada";
    private static final String REAGENDADA = "reagendada";
    private static final String CANCELADA = "cancelada";

    // Recargo de $10.000 por consulta de urgencia fuera de horario
    private static final int RECARGO_URGENCIA = 10000;
    private static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");

    private static final Set<String> ESTADOS = Set.of(PENDIENTE, CONFIRMADA, REAGENDADA, CANCELADA);
    private static final Set<String> ESPECIES = Set.of("perro", "gato", "conejo", "ave");
    private static final Set<String> HORARIOS = Set.of(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00",
            "15:00", "15:30", "16:00", "16:30", "17:00", "17:30");

    private final CitaRepository citas;
    private final UsuarioRepository usuarios;
    private final ServicioRepository servicios;
    private final int maximoPorHorario;

    public CitaService(CitaRepository citas, UsuarioRepository usuarios, ServicioRepository servicios,
                       @Value("${app.citas.maximo-por-horario:1}") int maximoPorHorario) {
        this.citas = citas;
        this.usuarios = usuarios;
        this.servicios = servicios;
        this.maximoPorHorario = maximoPorHorario;
    }

    // CREAR: la cita queda "pendiente" y a nombre del usuario del token
    public CitaResponse crear(CitaRequest datos, UsuarioAutenticado actor) {
        Usuario usuario = usuarios.findById(actor.id())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida o vencida"));
        Servicio servicio = servicios.findById(datos.servicioId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "El servicio indicado no existe"));

        String especie = datos.especie().trim().toLowerCase();
        if (!ESPECIES.contains(especie)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La especie debe ser perro, gato, conejo o ave");
        }
        LocalDate fecha = validarFecha(datos.fecha());
        String hora = validarHora(datos.hora());

        // La urgencia solo aplica a las consultas
        boolean urgencia = Boolean.TRUE.equals(datos.urgencia()) && "Consulta".equals(servicio.getCategoria());

        Cita cita = new Cita();
        cita.setUsuario(usuario);
        cita.setServicio(servicio);
        cita.setMascota(datos.mascota().trim());
        cita.setEspecie(especie);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(datos.motivo().trim());
        cita.setUrgencia(urgencia);
        cita.setPrecioFinal(calcularPrecio(servicio, urgencia));
        cita.setEstado(PENDIENTE);
        cita.setCreadaEn(LocalDateTime.now(ZONA_CHILE));
        return CitaResponse.desde(citas.save(cita));
    }

    // LISTAR: el personal ve todas; el dueño solo las suyas. Se puede filtrar por estado
    public List<CitaResponse> listar(UsuarioAutenticado actor, String estado) {
        List<Cita> base = esPersonal(actor)
                ? citas.findAllByOrderByFechaAscHoraAsc()
                : citas.findByUsuarioIdOrderByFechaAscHoraAsc(actor.id());

        if (estado != null && !estado.isBlank()) {
            if (!ESTADOS.contains(estado)) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "El estado debe ser pendiente, confirmada, reagendada o cancelada");
            }
            base = base.stream().filter(c -> c.getEstado().equals(estado)).toList();
        }
        return base.stream().map(CitaResponse::desde).toList();
    }

    // DETALLE: cambiar el id en la URL no deja ver la cita de otra persona
    public CitaResponse obtener(Long id, UsuarioAutenticado actor) {
        Cita cita = buscar(id);
        verificarAcceso(cita, actor);
        return CitaResponse.desde(cita);
    }

    // CONFIRMAR (personal): aquí está la regla de negocio que puede terminar en 409
    public CitaResponse confirmar(Long id) {
        Cita cita = buscar(id);
        exigirAbierta(cita, "confirmar");
        verificarHorarioLibre(cita.getFecha(), cita.getHora(), cita.getId());
        cita.setEstado(CONFIRMADA);
        return CitaResponse.desde(citas.save(cita));
    }

    // REAGENDAR (personal): propone otro horario, que tampoco puede estar ocupado
    public CitaResponse reagendar(Long id, ReagendarRequest datos) {
        Cita cita = buscar(id);
        exigirAbierta(cita, "reagendar");
        LocalDate fecha = validarFecha(datos.fecha());
        String hora = validarHora(datos.hora());
        verificarHorarioLibre(fecha, hora, cita.getId());
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado(REAGENDADA);
        return CitaResponse.desde(citas.save(cita));
    }

    // CANCELAR: el personal o el dueño de la cita
    public CitaResponse cancelar(Long id, UsuarioAutenticado actor) {
        Cita cita = buscar(id);
        verificarAcceso(cita, actor);
        if (CANCELADA.equals(cita.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "La cita ya está cancelada");
        }
        cita.setEstado(CANCELADA);
        return CitaResponse.desde(citas.save(cita));
    }

    // ---- métodos auxiliares ----

    // Regla de negocio: no se puede ocupar un horario que ya tiene citas confirmadas
    private void verificarHorarioLibre(LocalDate fecha, String hora, Long idCita) {
        long ocupadas = citas.countByFechaAndHoraAndEstadoAndIdNot(fecha, hora, CONFIRMADA, idCita);
        if (ocupadas >= maximoPorHorario) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Ese horario ya está ocupado por otra cita confirmada");
        }
    }

    // Solo se puede confirmar o reagendar una cita pendiente o reagendada
    private void exigirAbierta(Cita cita, String accion) {
        if (!PENDIENTE.equals(cita.getEstado()) && !REAGENDADA.equals(cita.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "No se puede " + accion + " una cita que está " + cita.getEstado());
        }
    }

    private int calcularPrecio(Servicio servicio, boolean urgencia) {
        boolean enOferta = Boolean.TRUE.equals(servicio.getOferta()) && servicio.getPrecioOferta() != null;
        int base = enOferta ? servicio.getPrecioOferta() : servicio.getPrecio();
        return urgencia ? base + RECARGO_URGENCIA : base;
    }

    private LocalDate validarFecha(String texto) {
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha no es válida");
        }
        if (fecha.isBefore(LocalDate.now(ZONA_CHILE))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha no puede ser anterior a hoy");
        }
        return fecha;
    }

    private String validarHora(String texto) {
        String hora = texto.trim();
        if (!HORARIOS.contains(hora)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La hora debe ser una de las horas de atención (09:00 a 12:00 y 15:00 a 17:30)");
        }
        return hora;
    }

    private boolean esPersonal(UsuarioAutenticado actor) {
        return "administrador".equals(actor.rol()) || "recepcionista".equals(actor.rol());
    }

    // El personal accede a cualquier cita; el dueño solo a las suyas
    private void verificarAcceso(Cita cita, UsuarioAutenticado actor) {
        if (!esPersonal(actor) && !cita.getUsuario().getId().equals(actor.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No puedes acceder a la cita de otra persona");
        }
    }

    private Cita buscar(Long id) {
        return citas.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    }
}