package pe.edu.upeu.systurismo.exception;

/** Se lanza cuando se busca, actualiza o elimina un registro que no existe. */
public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(String message) {
        super(message);
    }
}
