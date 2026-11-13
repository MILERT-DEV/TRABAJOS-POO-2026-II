package pe.edu.upeu.systurismo.repository;

import pe.edu.upeu.systurismo.repository.helper.PageResult;

import java.util.List;
import java.util.Optional;

public interface ICrudGenericoRepository<T, ID> {
    T save(T entity);
    T update(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    void deleteById(ID id);
    boolean existsById(ID id);

    /**
     * Consulta paginada con filtro de texto.
     *
     * @param filtro texto a buscar (vacío o null = sin filtro)
     * @param page   página, empieza en 0
     * @param size   registros por página
     */
    PageResult<T> findPage(String filtro, int page, int size);
}
