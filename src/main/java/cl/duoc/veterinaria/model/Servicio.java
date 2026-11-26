package cl.duoc.veterinaria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Cada fila de la tabla "servicios" es un servicio que ofrece la clínica
@Entity
@Table(name = "servicios")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código único del servicio, por ejemplo CON-001
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String especie;

    // Duración en minutos
    @Column(nullable = false)
    private Integer duracion;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false)
    private Boolean oferta = false;

    // Solo tiene valor cuando oferta es verdadero
    private Integer precioOferta;

    @Column(length = 500)
    private String observaciones;

    @Column(length = 200)
    private String imagen;

    public Servicio() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public Integer getPrecio() { return precio; }
    public void setPrecio(Integer precio) { this.precio = precio; }

    public Boolean getOferta() { return oferta; }
    public void setOferta(Boolean oferta) { this.oferta = oferta; }

    public Integer getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(Integer precioOferta) { this.precioOferta = precioOferta; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}