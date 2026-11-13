package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.exception.ValidacionException;
import pe.edu.upeu.systurismo.model.Usuario;
import pe.edu.upeu.systurismo.repository.ICrudGenericoRepository;
import pe.edu.upeu.systurismo.repository.UsuarioRepository;
import pe.edu.upeu.systurismo.service.IUsuarioService;
import pe.edu.upeu.systurismo.utils.PasswordHasher;
import pe.edu.upeu.systurismo.utils.ValidadorEntidades;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * La clave llega en texto plano desde la pantalla, se valida aquí y se guarda cifrada (PBKDF2).
 * Al editar, una clave vacía significa "no cambiarla".
 */
public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long> implements IUsuarioService {

    private static final int LARGO_MINIMO_CLAVE = 8;

    private final UsuarioRepository repository;

    public UsuarioServiceImp(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() {
        return repository;
    }

    @Override
    public Usuario save(Usuario usuario) {
        Map<String, String> errores = validarPrevio(usuario, true);
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        usuario.setClave(PasswordHasher.hash(usuario.getClave()));
        return super.save(usuario);
    }

    @Override
    public Usuario update(Long id, Usuario usuario) {
        boolean cambiaClave = usuario.getClave() != null && !usuario.getClave().isEmpty();
        Map<String, String> errores = validarPrevio(usuario, cambiaClave);
        if (!errores.isEmpty()) {
            throw new ValidacionException(errores);
        }
        if (cambiaClave) {
            usuario.setClave(PasswordHasher.hash(usuario.getClave()));
        } else {
            usuario.setClave(findById(id).getClave());
        }
        return super.update(id, usuario);
    }

    @Override
    public Optional<Usuario> autenticar(String usuario, String clave) {
        if (usuario == null || usuario.isBlank() || clave == null) {
            return Optional.empty();
        }
        return repository.buscarPorUsuario(usuario.trim())
                .filter(u -> u.getEstado() == EstadoRegistro.ACTIVO)
                .filter(u -> PasswordHasher.verificar(clave, u.getClave()));
    }

    @Override
    protected void reglasDeNegocio(Usuario u) {
        repository.buscarPorUsuario(u.getUsuario()).ifPresent(otro -> {
            if (!otro.getIdUsuario().equals(u.getIdUsuario())) {
                throw new ValidacionException("usuario", "Ese nombre de usuario ya está en uso");
            }
        });
    }

    private Map<String, String> validarPrevio(Usuario u, boolean validarClave) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (validarClave) {
            String clave = u.getClave();
            if (clave == null || clave.length() < LARGO_MINIMO_CLAVE) {
                errores.put("clave", "La contraseña debe tener al menos " + LARGO_MINIMO_CLAVE + " caracteres");
            } else if (clave.chars().noneMatch(Character::isLetter) || clave.chars().noneMatch(Character::isDigit)) {
                errores.put("clave", "La contraseña debe combinar letras y números");
            }
        }
        try {
            ValidadorEntidades.validar(u);
        } catch (ValidacionException e) {
            e.getErrores().forEach(errores::putIfAbsent);
        }
        return errores;
    }
}
