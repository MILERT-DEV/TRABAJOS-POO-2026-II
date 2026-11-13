package pe.edu.upeu.systurismo.exception;

/**
 * Se lanza cuando una operación incumple una regla del negocio
 * (por ejemplo: no hay cupos, el documento ya existe, la reserva ya fue cancelada).
 * El mensaje está escrito para mostrarse tal cual al usuario.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String message) {
        super(message);
    }
}
