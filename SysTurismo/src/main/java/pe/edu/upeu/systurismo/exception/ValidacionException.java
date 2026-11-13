package pe.edu.upeu.systurismo.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Errores de validación de campos. La clave es el nombre de la propiedad de la entidad
 * (por ejemplo "nombre" o "destino") y el valor es el mensaje para el usuario.
 */
public class ValidacionException extends RuntimeException {

    private final transient Map<String, String> errores;

    public ValidacionException(Map<String, String> errores) {
        super(errores.isEmpty() ? "Datos inválidos" : errores.values().iterator().next());
        this.errores = new LinkedHashMap<>(errores);
    }

    public ValidacionException(String campo, String mensaje) {
        this(Map.of(campo, mensaje));
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
