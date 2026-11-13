package pe.edu.upeu.systurismo.service;

import pe.edu.upeu.systurismo.repository.helper.PageResult;

import java.util.List;

public interface ICrudGenericoService<T, ID> {
    T save(T t);
    T update(ID id, T t);
    List<T> findAll();
    T findById(ID id);
    void delete(ID id);
    PageResult<T> findPage(String filtro, int page, int size);
}
