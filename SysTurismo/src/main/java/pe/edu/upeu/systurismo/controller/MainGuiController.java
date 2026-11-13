package pe.edu.upeu.systurismo.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.upeu.systurismo.SysTurismo;
import pe.edu.upeu.systurismo.components.Toast;
import pe.edu.upeu.systurismo.config.AppContext;
import pe.edu.upeu.systurismo.dto.ModuloMenu;
import pe.edu.upeu.systurismo.dto.SessionManager;
import pe.edu.upeu.systurismo.service.INavegacionService;
import pe.edu.upeu.systurismo.utils.FxUtil;

import java.util.List;

/** Ventana principal: menú lateral según el perfil y un panel central donde se cargan las pantallas. */
public class MainGuiController {

    private static final Logger LOG = LoggerFactory.getLogger(MainGuiController.class);

    private final INavegacionService navegacionService;

    @FXML private VBox navegacion;
    @FXML private StackPane contenido;
    @FXML private Label lblTituloModulo;
    @FXML private Label lblUsuario;
    @FXML private Label lblPerfil;
    @FXML private Label lblIniciales;

    public MainGuiController(INavegacionService navegacionService) {
        this.navegacionService = navegacionService;
    }

    @FXML
    public void initialize() {
        SessionManager sesion = SessionManager.getInstance();
        String nombre = sesion.getNombreCompleto() == null ? sesion.getUserName() : sesion.getNombreCompleto();
        lblUsuario.setText(nombre);
        lblPerfil.setText(sesion.getUserPerfil());
        lblIniciales.setText(nombre.substring(0, 1).toUpperCase());

        ToggleGroup grupo = new ToggleGroup();
        List<ModuloMenu> modulos = navegacionService.modulosPara(sesion.getUserPerfil());
        ToggleButton primero = null;
        for (ModuloMenu modulo : modulos) {
            ToggleButton boton = new ToggleButton(modulo.icono() + "   " + modulo.titulo());
            boton.setToggleGroup(grupo);
            boton.getStyleClass().add("nav-item");
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setOnAction(e -> {
                boton.setSelected(true);   // evita que el botón activo quede sin selección
                abrir(modulo);
            });
            navegacion.getChildren().add(boton);
            if (primero == null) {
                primero = boton;
            }
        }
        if (primero != null) {
            primero.setSelected(true);
            abrir(modulos.get(0));
        }
    }

    private void abrir(ModuloMenu modulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(modulo.fxml()));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Parent vista = loader.load();
            contenido.getChildren().setAll(vista);
            lblTituloModulo.setText(modulo.titulo());

            FadeTransition entrada = new FadeTransition(Duration.millis(220), vista);
            entrada.setFromValue(0);
            entrada.setToValue(1);
            entrada.play();
        } catch (Exception e) {
            LOG.error("No se pudo abrir {}", modulo.fxml(), e);
            Toast.error((Stage) contenido.getScene().getWindow(), "No se pudo abrir " + modulo.titulo());
        }
    }

    @FXML
    public void cerrarSesion() {
        Stage stage = (Stage) contenido.getScene().getWindow();
        if (FxUtil.confirmar(stage, "Cerrar sesión", "¿Desea salir de su cuenta?")) {
            SessionManager.getInstance().cerrar();
            SysTurismo.mostrarLogin(stage);
        }
    }
}
