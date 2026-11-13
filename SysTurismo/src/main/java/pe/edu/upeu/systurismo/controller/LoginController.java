package pe.edu.upeu.systurismo.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import pe.edu.upeu.systurismo.components.StageManager;
import pe.edu.upeu.systurismo.config.AppContext;
import pe.edu.upeu.systurismo.dto.SessionManager;
import pe.edu.upeu.systurismo.model.Usuario;
import pe.edu.upeu.systurismo.service.IUsuarioService;
import pe.edu.upeu.systurismo.utils.FxUtil;

public class LoginController {

    private final IUsuarioService usuarioService;

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblError;
    @FXML private Button btnIngresar;

    public LoginController(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    public void initialize() {
        Platform.runLater(txtUsuario::requestFocus);
        // Enter en el usuario pasa a la clave; Enter en la clave ingresa.
        txtUsuario.setOnAction(e -> txtClave.requestFocus());
        txtClave.setOnAction(e -> login());
    }

    @FXML
    public void login() {
        String usuario = txtUsuario.getText();
        String clave = txtClave.getText();
        if (usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            mostrarError("Ingrese su usuario y su contraseña");
            return;
        }
        btnIngresar.setDisable(true);
        try {
            usuarioService.autenticar(usuario, clave).ifPresentOrElse(
                    this::abrirPrincipal,
                    () -> {
                        mostrarError("Usuario o contraseña incorrectos");
                        txtClave.clear();
                        txtClave.requestFocus();
                    });
        } catch (RuntimeException e) {
            mostrarError("No se pudo iniciar sesión: " + e.getMessage());
        } finally {
            btnIngresar.setDisable(false);
        }
    }

    @FXML
    public void salir() {
        Platform.exit();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
        // Pequeño temblor horizontal del formulario para llamar la atención.
        Timeline temblor = new Timeline();
        for (int i = 0; i < 6; i++) {
            double desplazamiento = (i % 2 == 0 ? 8 : -8) * (1 - i / 6.0);
            temblor.getKeyFrames().add(new KeyFrame(Duration.millis(50 * (i + 1)),
                    e -> btnIngresar.getParent().setTranslateX(desplazamiento)));
        }
        temblor.getKeyFrames().add(new KeyFrame(Duration.millis(350), e -> btnIngresar.getParent().setTranslateX(0)));
        temblor.play();
    }

    private void abrirPrincipal(Usuario usuario) {
        SessionManager sesion = SessionManager.getInstance();
        sesion.setUserId(usuario.getIdUsuario());
        sesion.setUserName(usuario.getUsuario());
        sesion.setNombreCompleto(usuario.getNombreCompleto());
        sesion.setUserPerfil(usuario.getPerfil().getNombre());

        try {
            Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/maingui.fxml"));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Scene escena = new Scene(loader.load(), pantalla.getWidth(), pantalla.getHeight());
            escena.getStylesheets().add(FxUtil.cssTurismo());

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            StageManager.setPrimaryStage(stage);
            stage.setTitle("SysTurismo · Agencia de viajes");
            stage.setScene(escena);
            stage.setResizable(true);
            stage.setX(pantalla.getMinX());
            stage.setY(pantalla.getMinY());
            stage.setWidth(pantalla.getWidth());
            stage.setHeight(pantalla.getHeight());
        } catch (Exception e) {
            mostrarError("No se pudo abrir la pantalla principal: " + e.getMessage());
        }
    }
}
