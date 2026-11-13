package pe.edu.upeu.systurismo.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Function;

/** Pequeñas utilidades de interfaz que se repiten en varias pantallas. */
public final class FxUtil {

    private FxUtil() {
    }

    public static String cssTurismo() {
        return FxUtil.class.getResource("/css/turismo.css").toExternalForm();
    }

    /** Diálogo de confirmación. Devuelve true si el usuario aceptó. */
    public static boolean confirmar(Window owner, String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.getDialogPane().getStylesheets().add(cssTurismo());
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    // ---------------------------------------------------- restricciones de teclado

    /** Limita la cantidad de caracteres de un campo de texto o área de texto. */
    public static void limitarLongitud(TextInputControl control, int max) {
        control.setTextFormatter(new TextFormatter<String>(
                cambio -> cambio.getControlNewText().length() <= max ? cambio : null));
    }

    /** Solo acepta dígitos, hasta una longitud máxima. */
    public static void soloDigitos(TextField campo, int max) {
        campo.setTextFormatter(new TextFormatter<String>(
                cambio -> cambio.getControlNewText().matches("\\d{0," + max + "}") ? cambio : null));
    }

    /** Solo acepta letras y números (sin espacios), hasta una longitud máxima. */
    public static void soloAlfanumerico(TextField campo, int max) {
        campo.setTextFormatter(new TextFormatter<String>(
                cambio -> cambio.getControlNewText().matches("[A-Za-z0-9]{0," + max + "}") ? cambio : null));
    }

    /** Número decimal positivo con hasta dos decimales. */
    public static void soloDecimal(TextField campo, int maxEnteros) {
        campo.setTextFormatter(new TextFormatter<String>(
                cambio -> cambio.getControlNewText().matches("\\d{0," + maxEnteros + "}(\\.\\d{0,2})?") ? cambio : null));
    }

    // ---------------------------------------------------------------- combos

    /** Muestra en el ComboBox el texto que indique la función, en lugar del toString del objeto. */
    public static <T> void configurarCombo(ComboBox<T> combo, Function<T, String> texto) {
        StringConverter<T> convertidor = new StringConverter<>() {
            @Override
            public String toString(T objeto) {
                return objeto == null ? "" : texto.apply(objeto);
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        };
        combo.setConverter(convertidor);
    }

    /** Selecciona en el combo el elemento cuyo id coincide (null si no hay coincidencia). */
    public static <T, I> void seleccionarPorId(ComboBox<T> combo, I id, Function<T, I> idDe) {
        combo.setValue(combo.getItems().stream()
                .filter(x -> idDe.apply(x) != null && idDe.apply(x).equals(id))
                .findFirst()
                .orElse(null));
    }

    // ---------------------------------------------------------------- formato

    public static String moneda(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor;
        return "S/ " + String.format(Locale.US, "%,.2f", v);
    }
}
