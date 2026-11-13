package pe.edu.upeu.systurismo.repository;

import pe.edu.upeu.systurismo.repository.helper.PageResult;
import pe.edu.upeu.systurismo.repository.helper.SqlHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio base (patrón Template Method). Implementa el CRUD y la paginación una sola vez;
 * cada repositorio concreto solo dice cómo se llama su tabla, cómo se arma el SELECT,
 * en qué columnas se busca y cómo se inserta, actualiza y mapea una fila.
 *
 * <p>Nota: el nombre conserva "Jpa" por compatibilidad con la Sesión 11, pero usa JDBC puro.
 */
public abstract class AbstractJpaRepository<T, ID> extends SqlHelper<T>
        implements ICrudGenericoRepository<T, ID> {

    protected abstract String getTableName();

    protected abstract String getPkColumn();

    protected abstract T insert(Connection connection, T entity) throws SQLException;

    protected abstract T updateRow(Connection connection, T entity) throws SQLException;

    // ----- Puntos de extensión para repositorios con JOIN -----

    /** Columnas del SELECT. Por defecto, todas las de la tabla. */
    protected String selectColumns() {
        return getTableName() + ".*";
    }

    /** Cláusula FROM. Los repositorios con relaciones agregan sus JOIN aquí. */
    protected String fromClause() {
        return getTableName();
    }

    /** Columnas de texto (con su tabla) sobre las que busca el filtro. */
    protected List<String> searchColumns() {
        return List.of();
    }

    /** Orden de los resultados. Por defecto, lo más reciente primero. */
    protected String orderBy() {
        return getTableName() + "." + getPkColumn() + " DESC";
    }

    protected String baseSelect() {
        return "SELECT " + selectColumns() + " FROM " + fromClause();
    }

    // ----- CRUD -----

    @Override
    public T save(T entity) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = insert(conn, entity);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al guardar");
        }
    }

    @Override
    public T update(T entity) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = updateRow(conn, entity);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al actualizar");
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        return executeQueryOne(baseSelect() + " WHERE " + getTableName() + "." + getPkColumn() + " = ?", id);
    }

    @Override
    public List<T> findAll() {
        return executeQuery(baseSelect() + " ORDER BY " + orderBy());
    }

    @Override
    public void deleteById(ID id) {
        executeUpdateStandalone("DELETE FROM " + getTableName() + " WHERE " + getPkColumn() + " = ?", id);
    }

    @Override
    public boolean existsById(ID id) {
        return executeExists("SELECT 1 FROM " + getTableName() + " WHERE " + getPkColumn() + " = ?", id);
    }

    // ----- Paginación -----

    /**
     * La paginación se hace en la base de datos (LIMIT/OFFSET) y no en memoria:
     * solo viajan a la aplicación los registros de la página que se muestra.
     */
    @Override
    public PageResult<T> findPage(String filtro, int page, int size) {
        List<Object> params = new ArrayList<>();
        String where = construirWhere(filtro, params);

        long total = executeScalarLong("SELECT COUNT(*) FROM " + fromClause() + where, params.toArray());

        List<Object> paramsPagina = new ArrayList<>(params);
        paramsPagina.add(size);
        paramsPagina.add((long) page * size);
        List<T> items = executeQuery(
                baseSelect() + where + " ORDER BY " + orderBy() + " LIMIT ? OFFSET ?",
                paramsPagina.toArray());

        return new PageResult<>(items, total, page, size);
    }

    private String construirWhere(String filtro, List<Object> params) {
        if (filtro == null || filtro.isBlank() || searchColumns().isEmpty()) {
            return "";
        }
        String patron = "%" + filtro.trim().toLowerCase() + "%";
        StringBuilder sb = new StringBuilder(" WHERE ");
        for (int i = 0; i < searchColumns().size(); i++) {
            if (i > 0) {
                sb.append(" OR ");
            }
            sb.append("LOWER(").append(searchColumns().get(i)).append(") LIKE ?");
            params.add(patron);
        }
        return sb.toString();
    }
}
