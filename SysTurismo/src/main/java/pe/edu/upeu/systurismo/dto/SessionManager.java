package pe.edu.upeu.systurismo.dto;

import lombok.Data;

/** Datos del usuario que inició sesión (un solo usuario por ejecución). */
@Data
public class SessionManager {
    private static SessionManager instance;

    private Long userId;
    private String userName;
    private String nombreCompleto;
    private String userPerfil;

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void cerrar() {
        userId = null;
        userName = null;
        nombreCompleto = null;
        userPerfil = null;
    }
}
