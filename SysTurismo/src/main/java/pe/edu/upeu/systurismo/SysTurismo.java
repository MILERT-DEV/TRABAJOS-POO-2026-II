package pe.edu.upeu.systurismo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.upeu.systurismo.config.AppContext;
import pe.edu.upeu.systurismo.config.DataInitializer;
import pe.edu.upeu.systurismo.config.DatabaseConfig;
import pe.edu.upeu.systurismo.utils.FxUtil;

import java.io.IOException;

public class SysTurismo extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(SysTurismo.class);

    @Override
    public void start(Stage stage) throws IOException {
        AppContext contexto = AppContext.getInstance();
        contexto.getBean(DataInitializer.class).ejecutar();
        mostrarLogin(stage);
        stage.show();
    }

    /** Muestra la pantalla de inicio de sesión (también se usa al cerrar sesión). */
    public static void mostrarLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(SysTurismo.class.getResource("/view/login.fxml"));
            loader.setControllerFactory(AppContext.getInstance()::getBean);
            Scene escena = new Scene(loader.load());
            escena.getStylesheets().add(FxUtil.cssTurismo());
            stage.setMaximized(false);
            stage.setTitle("SysTurismo · Iniciar sesión");
            stage.setScene(escena);
            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (IOException e) {
            LOG.error("No se pudo abrir el login", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stop() {
        DatabaseConfig.shutdown();
        Platform.exit();
    }
}
