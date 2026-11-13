package pe.edu.upeu.systurismo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.upeu.systurismo.enums.Dificultad;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.enums.MetodoPago;
import pe.edu.upeu.systurismo.enums.TipoDestino;
import pe.edu.upeu.systurismo.enums.TipoDocumento;
import pe.edu.upeu.systurismo.model.Destino;
import pe.edu.upeu.systurismo.model.Guia;
import pe.edu.upeu.systurismo.model.Paquete;
import pe.edu.upeu.systurismo.model.Perfil;
import pe.edu.upeu.systurismo.model.Reserva;
import pe.edu.upeu.systurismo.model.Turista;
import pe.edu.upeu.systurismo.model.Usuario;
import pe.edu.upeu.systurismo.service.IDestinoService;
import pe.edu.upeu.systurismo.service.IGuiaService;
import pe.edu.upeu.systurismo.service.IPaqueteService;
import pe.edu.upeu.systurismo.service.IPerfilService;
import pe.edu.upeu.systurismo.service.IReservaService;
import pe.edu.upeu.systurismo.service.ITuristaService;
import pe.edu.upeu.systurismo.service.IUsuarioService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Carga los datos de demostración la primera vez que se ejecuta el sistema (tablas vacías).
 * Usa los mismos servicios que la interfaz, así que los datos pasan por las mismas validaciones.
 * Las claves se cifran al guardarse; las de demostración están en el README.
 */
public class DataInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    private final IPerfilService perfiles;
    private final IUsuarioService usuarios;
    private final IDestinoService destinos;
    private final IGuiaService guias;
    private final IPaqueteService paquetes;
    private final ITuristaService turistas;
    private final IReservaService reservas;

    public DataInitializer(IPerfilService perfiles, IUsuarioService usuarios, IDestinoService destinos,
                           IGuiaService guias, IPaqueteService paquetes, ITuristaService turistas,
                           IReservaService reservas) {
        this.perfiles = perfiles;
        this.usuarios = usuarios;
        this.destinos = destinos;
        this.guias = guias;
        this.paquetes = paquetes;
        this.turistas = turistas;
        this.reservas = reservas;
    }

    public void ejecutar() {
        if (usuarios.findAll().isEmpty()) {
            crearUsuarios();
        }
        if (destinos.findAll().isEmpty()) {
            crearCatalogoYReservas();
        }
    }

    private void crearUsuarios() {
        Perfil root = perfil("Root");
        Perfil agente = perfil("Agente");
        usuarios.save(Usuario.builder().usuario("admin").nombreCompleto("Administrador del sistema")
                .clave("Admin2026!").perfil(root).estado(EstadoRegistro.ACTIVO).build());
        usuarios.save(Usuario.builder().usuario("agente1").nombreCompleto("Marisol Apaza Cutipa")
                .clave("Agente2026!").perfil(agente).estado(EstadoRegistro.ACTIVO).build());
        LOG.info("Usuarios de demostración creados");
    }

    private Perfil perfil(String nombre) {
        return perfiles.findAll().stream().filter(p -> p.getNombre().equals(nombre)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Falta el perfil " + nombre));
    }

    private void crearCatalogoYReservas() {
        Destino machu = destino("Machu Picchu", "Cusco", TipoDestino.CULTURAL,
                "Ciudadela inca del siglo XV sobre el valle del Urubamba.");
        Destino colca = destino("Valle del Colca", "Arequipa", TipoDestino.AVENTURA,
                "Cañón profundo con andenes, miradores del cóndor y aguas termales.");
        Destino titicaca = destino("Lago Titicaca", "Puno", TipoDestino.NATURALEZA,
                "Lago navegable más alto del mundo, con las islas Uros y Taquile.");
        Destino paracas = destino("Paracas e Islas Ballestas", "Ica", TipoDestino.NATURALEZA,
                "Reserva marina con lobos de mar, pingüinos y aves guaneras.");
        Destino mancora = destino("Máncora", "Piura", TipoDestino.PLAYA,
                "Playas de aguas cálidas durante casi todo el año.");
        Destino tambopata = destino("Reserva de Tambopata", "Madre de Dios", TipoDestino.NATURALEZA,
                "Selva amazónica con collpas de loros y navegación por el río.");
        Destino lima = destino("Lima gastronómica", "Lima", TipoDestino.GASTRONOMIA,
                "Recorrido por mercados, cevicherías y restaurantes de cocina criolla.");

        Guia rosa = guia("Rosa Quispe Mamani", "40123456", "951234567", "rosa.quispe@sysTurismo.pe", "Español, Inglés, Quechua");
        Guia luis = guia("Luis Condori Flores", "41234567", "962345678", "luis.condori@sysTurismo.pe", "Español, Inglés");
        Guia ana = guia("Ana Huamán Torres", "42345678", "973456789", "ana.huaman@sysTurismo.pe", "Español, Inglés, Francés");
        Guia carlos = guia("Carlos Mendoza Ríos", "43456789", "984567890", "carlos.mendoza@sysTurismo.pe", "Español, Portugués");

        Paquete pMachu = paquete("Machu Picchu 4 días", machu, rosa, 4, "1850.00", 20, Dificultad.MODERADA,
                "Hotel, trenes, entradas, desayunos y guía.");
        Paquete pColcaExp = paquete("Colca Express 2 días", colca, luis, 2, "320.00", 25, Dificultad.FACIL,
                "Transporte, hotel, desayunos y baños termales.");
        Paquete pColcaTrek = paquete("Colca Trekking 3 días", colca, luis, 3, "480.00", 12, Dificultad.EXIGENTE,
                "Guía, carpa, alimentación y transporte.");
        Paquete pTiti = paquete("Titicaca e islas 2 días", titicaca, ana, 2, "390.00", 30, Dificultad.FACIL,
                "Bote, almuerzo en Taquile y hospedaje con familias.");
        Paquete pParacas = paquete("Paracas express", paracas, carlos, 1, "150.00", 40, Dificultad.FACIL,
                "Transporte, paseo en bote y entrada a la reserva.");
        paquete("Máncora relax 5 días", mancora, ana, 5, "1250.00", 18, Dificultad.FACIL,
                "Hotel frente al mar, desayunos y traslados.");
        Paquete pSelva = paquete("Selva Tambopata 4 días", tambopata, carlos, 4, "1480.00", 10, Dificultad.MODERADA,
                "Lodge, alimentación, bote y guía naturalista.");
        Paquete pLima = paquete("Sabores de Lima", lima, rosa, 1, "210.00", 15, Dificultad.FACIL,
                "Degustaciones, transporte y guía.");

        Turista t1 = turista(TipoDocumento.DNI, "45123678", "Jimena", "Cáceres Paredes", "jimena.caceres@correo.pe", "958123456", "Peruana");
        Turista t2 = turista(TipoDocumento.DNI, "46234789", "Diego", "Salazar Vilca", "diego.salazar@correo.pe", "947234567", "Peruana");
        Turista t3 = turista(TipoDocumento.PASAPORTE, "X4521897", "Emma", "Johnson", "emma.johnson@mail.com", "14155550134", "Estadounidense");
        Turista t4 = turista(TipoDocumento.PASAPORTE, "AB123456", "Lucas", "Martin", "lucas.martin@mail.fr", "33612345678", "Francesa");
        Turista t5 = turista(TipoDocumento.CARNET_EXTRANJERIA, "003456789", "Valentina", "Rojas Díaz", "valentina.rojas@correo.cl", "56912345678", "Chilena");
        Turista t6 = turista(TipoDocumento.DNI, "47345890", "Mateo", "Ccama Ticona", "mateo.ccama@correo.pe", "936345678", "Peruana");
        Turista t7 = turista(TipoDocumento.PASAPORTE, "C9087654", "Sofía", "Fernández", "sofia.fernandez@mail.ar", "5491145678901", "Argentina");
        turista(TipoDocumento.DNI, "48456901", "Renzo", "Apaza Lipa", "renzo.apaza@correo.pe", "925456789", "Peruana");

        Usuario registrador = usuarios.findAll().get(0);
        reservar(registrador, t1, pMachu, 21, 2, "1110.00", MetodoPago.TRANSFERENCIA);
        reservar(registrador, t3, pMachu, 35, 3, "1665.00", MetodoPago.TARJETA);
        reservar(registrador, t2, pColcaExp, 9, 4, "400.00", MetodoPago.YAPE_PLIN);
        reservar(registrador, t4, pTiti, 14, 2, "300.00", MetodoPago.TARJETA);
        reservar(registrador, t5, pSelva, 28, 2, "0", null);
        reservar(registrador, t6, pParacas, 5, 6, "300.00", MetodoPago.EFECTIVO);
        reservar(registrador, t7, pColcaTrek, 18, 3, "500.00", MetodoPago.TRANSFERENCIA);
        reservar(registrador, t1, pLima, 12, 2, "130.00", MetodoPago.EFECTIVO);
        Reserva cancelada = reservar(registrador, t2, pTiti, 30, 5, "0", null);
        reservas.cancelar(cancelada.getIdReserva());
        LOG.info("Catálogo y reservas de demostración creados");
    }

    private Destino destino(String nombre, String ciudad, TipoDestino tipo, String descripcion) {
        return destinos.save(Destino.builder().nombre(nombre).pais("Perú").ciudad(ciudad).tipoDestino(tipo)
                .descripcion(descripcion).estado(EstadoRegistro.ACTIVO).build());
    }

    private Guia guia(String nombres, String dni, String telefono, String email, String idiomas) {
        return guias.save(Guia.builder().nombres(nombres).dni(dni).telefono(telefono).email(email.toLowerCase())
                .idiomas(idiomas).estado(EstadoRegistro.ACTIVO).build());
    }

    private Paquete paquete(String nombre, Destino destino, Guia guia, int dias, String precio, int cupo,
                            Dificultad dificultad, String incluye) {
        return paquetes.save(Paquete.builder().nombre(nombre).destino(destino).guia(guia).duracionDias(dias)
                .precio(new BigDecimal(precio)).cupoMaximo(cupo).dificultad(dificultad).incluye(incluye)
                .estado(EstadoRegistro.ACTIVO).build());
    }

    private Turista turista(TipoDocumento tipo, String doc, String nombres, String apellidos, String email,
                            String telefono, String nacionalidad) {
        return turistas.save(Turista.builder().tipoDocumento(tipo).nroDocumento(doc).nombres(nombres)
                .apellidos(apellidos).email(email).telefono(telefono).nacionalidad(nacionalidad).build());
    }

    private Reserva reservar(Usuario usuario, Turista turista, Paquete paquete, int diasDesdeHoy, int personas,
                             String adelanto, MetodoPago metodo) {
        Reserva reserva = Reserva.builder().turista(turista).paquete(paquete)
                .fechaViaje(LocalDate.now().plusDays(diasDesdeHoy)).numPersonas(personas)
                .usuario(usuario).build();
        return reservas.reservar(reserva, new BigDecimal(adelanto), metodo);
    }
}
