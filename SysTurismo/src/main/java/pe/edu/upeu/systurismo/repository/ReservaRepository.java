package pe.edu.upeu.systurismo.repository;

import pe.edu.upeu.systurismo.enums.EstadoReserva;
import pe.edu.upeu.systurismo.exception.ReglaNegocioException;
import pe.edu.upeu.systurismo.model.Pago;
import pe.edu.upeu.systurismo.model.Paquete;
import pe.edu.upeu.systurismo.model.Reserva;
import pe.edu.upeu.systurismo.model.Turista;
import pe.edu.upeu.systurismo.model.Usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservaRepository extends AbstractJpaRepository<Reserva, Long> {

    @Override
    protected String getTableName() {
        return "reserva";
    }

    @Override
    protected String getPkColumn() {
        return "id_reserva";
    }

    @Override
    protected String selectColumns() {
        return "reserva.*, turista.nombres AS tur_nombres, turista.apellidos AS tur_apellidos, "
                + "turista.nro_documento AS tur_doc, paquete.nombre AS paq_nombre, paquete.precio AS paq_precio, "
                + "paquete.cupo_maximo AS paq_cupo, "
                + "(SELECT COALESCE(SUM(pago.monto), 0) FROM pago WHERE pago.id_reserva = reserva.id_reserva) AS pagado";
    }

    @Override
    protected String fromClause() {
        return "reserva JOIN turista ON reserva.id_turista = turista.id_turista "
                + "JOIN paquete ON reserva.id_paquete = paquete.id_paquete";
    }

    @Override
    protected List<String> searchColumns() {
        return List.of("turista.nombres", "turista.apellidos", "turista.nro_documento", "paquete.nombre");
    }

    @Override
    protected Reserva mapRow(ResultSet rs) throws SQLException {
        return Reserva.builder()
                .idReserva(rs.getLong("id_reserva"))
                .turista(Turista.builder().idTurista(rs.getLong("id_turista"))
                        .nombres(rs.getString("tur_nombres")).apellidos(rs.getString("tur_apellidos"))
                        .nroDocumento(rs.getString("tur_doc")).build())
                .paquete(Paquete.builder().idPaquete(rs.getLong("id_paquete"))
                        .nombre(rs.getString("paq_nombre")).precio(rs.getBigDecimal("paq_precio"))
                        .cupoMaximo(rs.getInt("paq_cupo")).build())
                .usuario(Usuario.builder().idUsuario(rs.getLong("id_usuario")).build())
                .fechaReserva(rs.getDate("fecha_reserva").toLocalDate())
                .fechaViaje(rs.getDate("fecha_viaje").toLocalDate())
                .numPersonas(rs.getInt("num_personas"))
                .precioTotal(rs.getBigDecimal("precio_total"))
                .estado(EstadoReserva.valueOf(rs.getString("estado")))
                .pagado(rs.getBigDecimal("pagado"))
                .build();
    }

    @Override
    protected Reserva insert(Connection conn, Reserva e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO reserva(id_turista,id_paquete,id_usuario,fecha_reserva,fecha_viaje,num_personas,precio_total,estado) "
                        + "VALUES(?,?,?,?,?,?,?,?)",
                e.getTurista().getIdTurista(), e.getPaquete().getIdPaquete(), e.getUsuario().getIdUsuario(),
                e.getFechaReserva(), e.getFechaViaje(), e.getNumPersonas(), e.getPrecioTotal(), e.getEstado().name());
        e.setIdReserva(id);
        return e;
    }

    @Override
    protected Reserva updateRow(Connection conn, Reserva e) throws SQLException {
        executeUpdate(conn, "UPDATE reserva SET estado=? WHERE id_reserva=?", e.getEstado().name(), e.getIdReserva());
        return e;
    }

    // ------------------------------------------------------------ consultas

    /** Personas ya reservadas (sin contar canceladas) para un paquete en una fecha. */
    public int personasReservadas(Long idPaquete, LocalDate fechaViaje) {
        return (int) executeScalarLong(
                "SELECT COALESCE(SUM(num_personas), 0) FROM reserva "
                        + "WHERE id_paquete = ? AND fecha_viaje = ? AND estado <> 'CANCELADA'",
                idPaquete, fechaViaje);
    }

    // --------------------------------------------------------- transacciones

    /**
     * Registra la reserva y su pago inicial en UNA transaccion: o se guardan las dos cosas o ninguna.
     * Antes de contar los cupos se bloquea la fila del paquete (FOR UPDATE), así dos reservas
     * simultáneas no pueden vender el mismo último cupo.
     */
    public Reserva registrarConPago(Reserva reserva, Pago pagoInicial) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                long cupo = executeScalarLong(conn,
                        "SELECT cupo_maximo FROM paquete WHERE id_paquete = ? FOR UPDATE",
                        reserva.getPaquete().getIdPaquete());
                long ocupados = executeScalarLong(conn,
                        "SELECT COALESCE(SUM(num_personas), 0) FROM reserva "
                                + "WHERE id_paquete = ? AND fecha_viaje = ? AND estado <> 'CANCELADA'",
                        reserva.getPaquete().getIdPaquete(), reserva.getFechaViaje());
                long libres = cupo - ocupados;
                if (reserva.getNumPersonas() > libres) {
                    throw new ReglaNegocioException("No hay cupos suficientes para esa fecha. Disponibles: "
                            + Math.max(libres, 0));
                }
                insert(conn, reserva);
                if (pagoInicial != null) {
                    pagoInicial.setIdReserva(reserva.getIdReserva());
                    insertarPago(conn, pagoInicial);
                }
                conn.commit();
                return reserva;
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al registrar la reserva");
        }
    }

    /** Inserta un pago y actualiza el estado de la reserva, también en una sola transacción. */
    public void registrarPago(Pago pago, EstadoReserva nuevoEstado) {
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try {
                insertarPago(conn, pago);
                executeUpdate(conn, "UPDATE reserva SET estado = ? WHERE id_reserva = ?",
                        nuevoEstado.name(), pago.getIdReserva());
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "Error al registrar el pago");
        }
    }

    public void cambiarEstado(Long idReserva, EstadoReserva estado) {
        executeUpdateStandalone("UPDATE reserva SET estado = ? WHERE id_reserva = ?", estado.name(), idReserva);
    }

    private void insertarPago(Connection conn, Pago p) throws SQLException {
        executeInsertGetKey(conn,
                "INSERT INTO pago(id_reserva,monto,metodo,fecha_pago) VALUES(?,?,?,?)",
                p.getIdReserva(), p.getMonto(), p.getMetodo().name(), p.getFechaPago());
    }
}
