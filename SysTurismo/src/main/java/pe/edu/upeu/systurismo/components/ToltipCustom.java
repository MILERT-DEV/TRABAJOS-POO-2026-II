package pe.edu.upeu.systurismo.components;

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Marca un campo con error: borde rojo (clase CSS "campo-error") y un tooltip con el mensaje.
 * El tooltip instalado se guarda en las propiedades del control para poder quitarlo después.
 */
public class ToltipCustom {

    private static final String CLASE_ERROR = "campo-error";
    private static final String CLAVE_TOOLTIP = "tooltip-error";

    public void marcarError(Control campo, String mensaje) {
        limpiarCampo(campo);
        campo.getStyleClass().add(CLASE_ERROR);

        Tooltip tooltip = new Tooltip("⚠  " + mensaje);
        tooltip.getStyleClass().add("tooltip-error");
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(200));
        tooltip.setShowDuration(Duration.seconds(10));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);
        Tooltip.install(campo, tooltip);
        campo.getProperties().put(CLAVE_TOOLTIP, tooltip);
    }

    public void limpiarCampo(Control campo) {
        campo.getStyleClass().remove(CLASE_ERROR);
        Object previo = campo.getProperties().remove(CLAVE_TOOLTIP);
        if (previo instanceof Tooltip tooltip) {
            Tooltip.uninstall(campo, tooltip);
        }
    }
}
