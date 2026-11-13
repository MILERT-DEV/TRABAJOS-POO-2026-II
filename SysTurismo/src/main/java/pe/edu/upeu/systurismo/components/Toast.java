package pe.edu.upeu.systurismo.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import pe.edu.upeu.systurismo.utils.FxUtil;

/** Mensaje emergente que aparece arriba a la derecha y desaparece solo. */
public final class Toast {

    public enum Tipo { EXITO, ERROR, AVISO }

    private static final int DURACION_MS = 2800;

    private Toast() {
    }

    public static void exito(Stage owner, String mensaje) {
        mostrar(owner, mensaje, Tipo.EXITO);
    }

    public static void error(Stage owner, String mensaje) {
        mostrar(owner, mensaje, Tipo.ERROR);
    }

    public static void aviso(Stage owner, String mensaje) {
        mostrar(owner, mensaje, Tipo.AVISO);
    }

    public static void mostrar(Stage owner, String mensaje, Tipo tipo) {
        if (owner == null) {
            return;
        }
        Label label = new Label(mensaje);
        label.setWrapText(true);
        label.setMaxWidth(380);
        label.getStyleClass().add("toast");
        label.getStyleClass().add(switch (tipo) {
            case EXITO -> "toast-exito";
            case ERROR -> "toast-error";
            case AVISO -> "toast-aviso";
        });

        // El StackPane deja margen para que se vea la sombra y lleva la hoja de estilos,
        // porque un Popup no hereda el CSS de la escena.
        StackPane pane = new StackPane(label);
        pane.setPadding(new Insets(14));
        pane.getStylesheets().add(FxUtil.cssTurismo());
        pane.setOpacity(0);

        Popup popup = new Popup();
        popup.getContent().add(pane);
        popup.show(owner, owner.getX() + owner.getWidth() - 440, owner.getY() + 70);
        // Cuando ya se conoce el ancho real, se alinea al borde derecho de la ventana.
        Platform.runLater(() -> popup.setX(owner.getX() + owner.getWidth() - pane.getWidth() - 20));

        Timeline entrada = new Timeline(new KeyFrame(Duration.millis(250), new KeyValue(pane.opacityProperty(), 1)));
        Timeline salida = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(pane.opacityProperty(), 0)));
        PauseTransition espera = new PauseTransition(Duration.millis(DURACION_MS));
        entrada.setOnFinished(e -> espera.play());
        espera.setOnFinished(e -> salida.play());
        salida.setOnFinished(e -> popup.hide());
        entrada.play();
    }
}
