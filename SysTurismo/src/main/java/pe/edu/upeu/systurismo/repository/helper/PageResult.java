package pe.edu.upeu.systurismo.repository.helper;

import java.util.List;

/**
 * Resultado de una consulta paginada.
 *
 * @param items elementos de la página actual
 * @param total cantidad total de registros que cumplen el filtro
 * @param page  número de página (empieza en 0)
 * @param size  tamaño de página
 */
public record PageResult<T>(List<T> items, long total, int page, int size) {

    public int totalPages() {
        return (int) Math.max(1, (total + size - 1) / size);
    }
}
