package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.exception.ModelNotFoundException;
import pe.edu.upeu.systurismo.repository.ICrudGenericoRepository;
import pe.edu.upeu.systurismo.repository.helper.PageResult;
import pe.edu.upeu.systurismo.service.ICrudGenericoService;
import pe.edu.upeu.systurismo.utils.ValidadorEntidades;

import java.util.List;

/**
 * Servicio base. Aquí viven las reglas comunes: toda entidad se valida antes de guardar
 * y se comprueba que exista antes de actualizar o eliminar.
 * Los servicios concretos agregan sus reglas propias sobrescribiendo {@link #reglasDeNegocio}.
 */
public abstract class CrudGenericoServiceImp<T, ID> implements ICrudGenericoService<T, ID> {

    protected abstract ICrudGenericoRepository<T, ID> getRepo();

    /** Punto de extensión: reglas que dependen de más de un campo o de otros registros. */
    protected void reglasDeNegocio(T t) {
    }

    @Override
    public T save(T t) {
        ValidadorEntidades.validar(t);
        reglasDeNegocio(t);
        return getRepo().save(t);
    }

    @Override
    public T update(ID id, T t) {
        if (!getRepo().existsById(id)) {
            throw new ModelNotFoundException("El registro ya no existe (id " + id + ")");
        }
        ValidadorEntidades.validar(t);
        reglasDeNegocio(t);
        return getRepo().update(t);
    }

    @Override
    public List<T> findAll() {
        return getRepo().findAll();
    }

    @Override
    public T findById(ID id) {
        return getRepo().findById(id)
                .orElseThrow(() -> new ModelNotFoundException("El registro no existe (id " + id + ")"));
    }

    @Override
    public void delete(ID id) {
        if (!getRepo().existsById(id)) {
            throw new ModelNotFoundException("El registro ya no existe (id " + id + ")");
        }
        getRepo().deleteById(id);
    }

    @Override
    public PageResult<T> findPage(String filtro, int page, int size) {
        return getRepo().findPage(filtro, page, size);
    }
}
