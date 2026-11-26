package cl.duoc.veterinaria.config;

import cl.duoc.veterinaria.model.Servicio;
import cl.duoc.veterinaria.model.Usuario;
import cl.duoc.veterinaria.repository.ServicioRepository;
import cl.duoc.veterinaria.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Se ejecuta al arrancar: si las tablas están vacías, crea los datos de demostración
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final ServicioRepository servicios;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarios, ServicioRepository servicios,
                           PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.servicios = servicios;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() == 0) {
            crearUsuario("Administrador San Marcos", "admin@sanmarcos.cl", "admin123", "administrador");
            crearUsuario("Recepcionista San Marcos", "recepcion@sanmarcos.cl", "recep123", "recepcionista");
            crearUsuario("Juan Pérez", "juan@correo.cl", "dueno123", "dueno");
        }
        if (servicios.count() == 0) {
            crearServicio("CON-001", "Consulta", "Consulta general", "Todas", 30, 18000, false, null,
                    "Examen físico y diagnóstico. La urgencia fuera de horario suma $10.000.",
                    "/Imagenes/ConsultaGeneral.jpg");
            crearServicio("PRE-001", "Prevención", "Vacunación", "Perro y gato", 20, 15000, true, 12000,
                    "Incluye registro en el carnet de vacunas.",
                    "/Imagenes/Vacunacion.jpg");
            crearServicio("PRE-002", "Prevención", "Desparasitación", "Todas", 15, 9000, true, 7000,
                    "Interna y externa, según el peso de la mascota.",
                    "/Imagenes/Desparacitacion.jpg");
            crearServicio("CIR-001", "Cirugía", "Cirugía menor", "Perro y gato", 90, 60000, false, null,
                    "Procedimientos quirúrgicos en pabellón equipado.",
                    "/Imagenes/Cirujia.jpg");
            crearServicio("CON-002", "Consulta", "Control de peso", "Perro y gato", 30, 12000, false, null,
                    "Evaluación nutricional y plan de alimentación.",
                    "/Imagenes/Pesaje.jpg");
        }
    }

    private void crearUsuario(String nombre, String correo, String contrasena, String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setRol(rol);
        usuarios.save(usuario);
    }

    private void crearServicio(String codigo, String categoria, String nombre, String especie,
                               int duracion, int precio, boolean oferta, Integer precioOferta,
                               String observaciones, String imagen) {
        Servicio servicio = new Servicio();
        servicio.setCodigo(codigo);
        servicio.setCategoria(categoria);
        servicio.setNombre(nombre);
        servicio.setEspecie(especie);
        servicio.setDuracion(duracion);
        servicio.setPrecio(precio);
        servicio.setOferta(oferta);
        servicio.setPrecioOferta(precioOferta);
        servicio.setObservaciones(observaciones);
        servicio.setImagen(imagen);
        servicios.save(servicio);
    }
}