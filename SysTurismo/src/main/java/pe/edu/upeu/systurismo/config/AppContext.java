package pe.edu.upeu.systurismo.config;

import pe.edu.upeu.systurismo.controller.*;
import pe.edu.upeu.systurismo.repository.*;
import pe.edu.upeu.systurismo.service.*;
import pe.edu.upeu.systurismo.service.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Contenedor de dependencias hecho a mano (inyección por constructor, sin frameworks).
 *
 * <ul>
 *   <li>Repositorios y servicios son únicos (singleton): se crean una vez y se reutilizan.</li>
 *   <li>Los controladores se crean de nuevo cada vez que se abre su pantalla (fábrica),
 *       así no arrastran el estado de una apertura anterior.</li>
 * </ul>
 * Las capas se conectan siempre en un solo sentido: controlador -> servicio -> repositorio.
 */
public class AppContext {

    private static AppContext instance;

    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }

    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<Object>> fabricas = new HashMap<>();

    private AppContext() {
        DatabaseConfig.init();
        registrarRepositorios();
        registrarServicios();
        registrarControladores();
    }

    // CAPA DE ACCESO A DATOS
    private void registrarRepositorios() {
        registrar(PerfilRepository.class, new PerfilRepository());
        registrar(UsuarioRepository.class, new UsuarioRepository());
        registrar(DestinoRepository.class, new DestinoRepository());
        registrar(GuiaRepository.class, new GuiaRepository());
        registrar(PaqueteRepository.class, new PaqueteRepository());
        registrar(TuristaRepository.class, new TuristaRepository());
        registrar(ReservaRepository.class, new ReservaRepository());
        registrar(DashboardRepository.class, new DashboardRepository());
    }

    // CAPA DE LÓGICA DE NEGOCIO
    private void registrarServicios() {
        registrar(IPerfilService.class, new PerfilServiceImp(getBean(PerfilRepository.class)));
        registrar(IUsuarioService.class, new UsuarioServiceImp(getBean(UsuarioRepository.class)));
        registrar(IDestinoService.class, new DestinoServiceImp(getBean(DestinoRepository.class)));
        registrar(IGuiaService.class, new GuiaServiceImp(getBean(GuiaRepository.class)));
        registrar(IPaqueteService.class, new PaqueteServiceImp(getBean(PaqueteRepository.class)));
        registrar(ITuristaService.class, new TuristaServiceImp(getBean(TuristaRepository.class)));
        registrar(IReservaService.class, new ReservaServiceImp(getBean(ReservaRepository.class)));
        registrar(IDashboardService.class, new DashboardServiceImp(getBean(DashboardRepository.class)));
        registrar(INavegacionService.class, new NavegacionServiceImp());
        registrar(DataInitializer.class, new DataInitializer(
                getBean(IPerfilService.class), getBean(IUsuarioService.class), getBean(IDestinoService.class),
                getBean(IGuiaService.class), getBean(IPaqueteService.class), getBean(ITuristaService.class),
                getBean(IReservaService.class)));
    }

    // CAPA DE PRESENTACIÓN (controladores JavaFX)
    private void registrarControladores() {
        fabrica(LoginController.class, () -> new LoginController(getBean(IUsuarioService.class)));
        fabrica(MainGuiController.class, () -> new MainGuiController(getBean(INavegacionService.class)));
        fabrica(DashboardController.class, () -> new DashboardController(getBean(IDashboardService.class)));
        fabrica(DestinoController.class, () -> new DestinoController(getBean(IDestinoService.class)));
        fabrica(GuiaController.class, () -> new GuiaController(getBean(IGuiaService.class)));
        fabrica(PaqueteController.class, () -> new PaqueteController(
                getBean(IPaqueteService.class), getBean(IDestinoService.class), getBean(IGuiaService.class)));
        fabrica(TuristaController.class, () -> new TuristaController(getBean(ITuristaService.class)));
        fabrica(UsuarioController.class, () -> new UsuarioController(
                getBean(IUsuarioService.class), getBean(IPerfilService.class)));
        fabrica(ReservaController.class, () -> new ReservaController(
                getBean(IReservaService.class), getBean(ITuristaService.class), getBean(IPaqueteService.class)));
    }

    private void registrar(Class<?> tipo, Object bean) {
        singletons.put(tipo, bean);
    }

    private void fabrica(Class<?> tipo, Supplier<Object> creador) {
        fabricas.put(tipo, creador);
    }

    /** Devuelve el objeto registrado para una clase o interfaz. FXMLLoader lo usa para crear controladores. */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> tipo) {
        Object bean = singletons.get(tipo);
        if (bean != null) {
            return (T) bean;
        }
        Supplier<Object> creador = fabricas.get(tipo);
        if (creador != null) {
            return (T) creador.get();
        }
        throw new IllegalStateException("No está registrado en AppContext: " + tipo.getName());
    }
}
