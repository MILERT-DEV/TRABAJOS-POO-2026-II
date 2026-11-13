package pe.edu.upeu.systurismo.repository.helper;

import pe.edu.upeu.systurismo.config.DatabaseConfig;
import pe.edu.upeu.systurismo.exception.ReglaNegocioException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Capa de acceso a datos con JDBC puro. Todas las consultas usan PreparedStatement
 * con parámetros (?), nunca se concatenan valores del usuario dentro del SQL.
 */
public abstract class SqlHelper<T> {

    protected Connection openConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    /** Convierte una fila del ResultSet en la entidad. Lo implementa cada repositorio. */
    protected abstract T mapRow(ResultSet rs) throws SQLException;

    // ---------------------------------------------------------------- SELECT

    protected List<T> executeQuery(String sql, Object... params) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw traducir(e, "Error en la consulta");
        }
    }

    protected Optional<T> executeQueryOne(String sql, Object... params) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traducir(e, "Error en la consulta");
        }
    }

    /** SELECT 1 FROM ... WHERE pk = ? (no usa mapRow). */
    protected boolean executeExists(String sql, Object id) {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al verificar existencia");
        }
    }

    /** Ejecuta un SELECT que devuelve un solo número (COUNT, SUM...). Abre su propia conexión. */
    protected long executeScalarLong(String sql, Object... params) {
        try (Connection conn = openConnection()) {
            return executeScalarLong(conn, sql, params);
        } catch (SQLException e) {
            throw traducir(e, "Error en la consulta");
        }
    }

    /** Igual que el anterior, pero dentro de una transacción ya abierta. */
    protected long executeScalarLong(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    // -------------------------------------------------- INSERT / UPDATE / DELETE

    /** INSERT dentro de una transacción abierta. Devuelve el ID generado por la base. */
    protected long executeInsertGetKey(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("No se generó clave para: " + sql);
            }
        }
    }

    /** UPDATE o DELETE dentro de una transacción abierta. Devuelve las filas afectadas. */
    protected int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    /** UPDATE o DELETE con su propia conexión y transacción. */
    protected int executeUpdateStandalone(String sql, Object... params) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                int rows = executeUpdate(conn, sql, params);
                conn.commit();
                return rows;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al modificar datos");
        }
    }

    // ------------------------------------------------------------- errores

    /**
     * Traduce los errores de integridad de H2 a mensajes que el usuario entiende.
     * SQLState 23505 = valor duplicado (UNIQUE). 23503 y 23506 = clave foránea.
     */
    protected RuntimeException traducir(SQLException e, String contexto) {
        String estado = e.getSQLState();
        if ("23505".equals(estado)) {
            return new ReglaNegocioException(
                    "Ya existe un registro con ese valor único (por ejemplo, el documento o el usuario).");
        }
        if ("23503".equals(estado) || "23506".equals(estado)) {
            return new ReglaNegocioException(
                    "No se puede completar la operación: el registro está relacionado con otros datos.");
        }
        return new RuntimeException(contexto + ": " + e.getMessage(), e);
    }

    private void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
