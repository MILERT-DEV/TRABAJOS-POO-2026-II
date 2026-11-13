package pe.edu.upeu.systurismo.service;

import pe.edu.upeu.systurismo.model.Usuario;

import java.util.Optional;

public interface IUsuarioService extends ICrudGenericoService<Usuario, Long> {
    /** Devuelve el usuario si existe, está activo y la clave coincide con el hash guardado. */
    Optional<Usuario> autenticar(String usuario, String clave);
}
