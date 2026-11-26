package cl.duoc.veterinaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Cada fila de la tabla "citas" es una solicitud de atención
// Relaciones: un usuario tiene muchas citas, y un servicio tiene muchas citas
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quién solicitó la cita (el servidor lo toma del token)
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Column(nullable = false, length = 100)
    private String mascota;

    @Column(nullable = false, length = 20)
    private String especie;

    @Column(nullable = false)
    private LocalDate fecha;

    // Formato HH:mm, por ejemplo 10:30
    @Column(nullable = false, length = 5)
    private String hora;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false)
    private Boolean urgencia = false;

    // Precio calculado por el servidor al solicitar la cita
    @Column(nullable = false)
    private Integer precioFinal;

    // pendiente, confirmada, reagendada o cancelada
    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime creadaEn;

    public Cita() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Servicio getServicio() { return servicio; }
    public void setServicio(Servicio servicio) { this.servicio = servicio; }

    public String getMascota() { return mascota; }
    public void setMascota(String mascota) { this.mascota = mascota; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public Boolean getUrgencia() { return urgencia; }
    public void setUrgencia(Boolean urgencia) { this.urgencia = urgencia; }

    public Integer getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(Integer precioFinal) { this.precioFinal = precioFinal; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }
}