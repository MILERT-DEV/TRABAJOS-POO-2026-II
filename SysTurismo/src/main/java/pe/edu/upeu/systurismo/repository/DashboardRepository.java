package pe.edu.upeu.systurismo.repository;

import pe.edu.upeu.systurismo.config.DatabaseConfig;
import pe.edu.upeu.systurismo.dto.ResumenDashboard;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultas de solo lectura para el panel principal (agregaciones, no entidades). */
public class DashboardRepository {

    public ResumenDashboard obtenerResumen() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            ResumenDashboard r = new ResumenDashboard();
            r.setTotalTuristas(contar(conn, "SELECT COUNT(*) FROM turista"));
            r.setPaquetesActivos(contar(conn, "SELECT COUNT(*) FROM paquete WHERE estado = 'ACTIVO'"));
            r.setReservasDelMes(contar(conn,
                    "SELECT COUNT(*) FROM reserva WHERE fecha_reserva >= ? AND estado <> 'CANCELADA'",
                    LocalDate.now().withDayOfMonth(1)));
            r.setIngresosCobrados(sumar(conn, "SELECT COALESCE(SUM(monto), 0) FROM pago"));
            r.setReservasPorEstado(estados(conn));
            r.setIngresosPorPaquete(ingresosPorPaquete(conn));
            r.setProximasSalidas(proximasSalidas(conn));
            return r;
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo cargar el resumen: " + e.getMessage(), e);
        }
    }

    private long contar(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private BigDecimal sumar(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    private Map<String, Long> estados(Connection conn) throws SQLException {
        Map<String, Long> mapa = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT estado, COUNT(*) AS cantidad FROM reserva GROUP BY estado ORDER BY estado");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString("estado"), rs.getLong("cantidad"));
            }
        }
        return mapa;
    }

    private Map<String, BigDecimal> ingresosPorPaquete(Connection conn) throws SQLException {
        Map<String, BigDecimal> mapa = new LinkedHashMap<>();
        String sql = "SELECT paquete.nombre AS nombre, SUM(pago.monto) AS total FROM pago "
                + "JOIN reserva ON pago.id_reserva = reserva.id_reserva "
                + "JOIN paquete ON reserva.id_paquete = paquete.id_paquete "
                + "GROUP BY paquete.nombre ORDER BY total DESC LIMIT 5";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString("nombre"), rs.getBigDecimal("total"));
            }
        }
        return mapa;
    }

    private List<ResumenDashboard.Salida> proximasSalidas(Connection conn) throws SQLException {
        List<ResumenDashboard.Salida> lista = new ArrayList<>();
        String sql = "SELECT reserva.fecha_viaje AS fecha, paquete.nombre AS nombre, SUM(reserva.num_personas) AS personas "
                + "FROM reserva JOIN paquete ON reserva.id_paquete = paquete.id_paquete "
                + "WHERE reserva.estado <> 'CANCELADA' AND reserva.fecha_viaje >= ? "
                + "GROUP BY reserva.fecha_viaje, paquete.nombre ORDER BY reserva.fecha_viaje LIMIT 5";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, LocalDate.now());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResumenDashboard.Salida(rs.getDate("fecha").toLocalDate(),
                            rs.getString("nombre"), rs.getLong("personas")));
                }
            }
        }
        return lista;
    }
}
