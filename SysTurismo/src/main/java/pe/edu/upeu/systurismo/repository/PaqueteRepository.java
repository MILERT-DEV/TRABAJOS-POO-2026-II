package pe.edu.upeu.systurismo.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import pe.edu.upeu.systurismo.enums.Dificultad;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.model.Destino;
import pe.edu.upeu.systurismo.model.Guia;
import pe.edu.upeu.systurismo.model.Paquete;

public class PaqueteRepository extends AbstractJpaRepository<Paquete, Long> {

    @Override
    protected String getTableName() {
        return "paquete";
    }

    @Override
    protected String getPkColumn() {
        return "id_paquete";
    }

    @Override
    protected List<String> searchColumns() {
        return List.of("paquete.nombre", "destino.nombre", "destino.ciudad", "guia.nombres");
    }

    @Override
    protected String selectColumns() {
        return "paquete.*, destino.nombre AS destino_nombre, destino.ciudad AS destino_ciudad, guia.nombres AS guia_nombres";
    }

    @Override
    protected String fromClause() {
        return "paquete JOIN destino ON paquete.id_destino = destino.id_destino JOIN guia ON paquete.id_guia = guia.id_guia";
    }

    @Override
    protected Paquete mapRow(ResultSet rs) throws SQLException {
        return Paquete.builder()
                .idPaquete(rs.getLong("id_paquete"))
                .nombre(rs.getString("nombre"))
                .destino(Destino.builder().idDestino(rs.getLong("id_destino")).nombre(rs.getString("destino_nombre")).ciudad(rs.getString("destino_ciudad")).build())
                .guia(Guia.builder().idGuia(rs.getLong("id_guia")).nombres(rs.getString("guia_nombres")).build())
                .duracionDias(rs.getInt("duracion_dias"))
                .cupoMaximo(rs.getInt("cupo_maximo"))
                .precio(rs.getBigDecimal("precio"))
                .dificultad(Dificultad.valueOf(rs.getString("dificultad")))
                .incluye(rs.getString("incluye"))
                .estado(EstadoRegistro.valueOf(rs.getString("estado")))
                .build();
    }

    @Override
    protected Paquete insert(Connection conn, Paquete e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO paquete(nombre,id_destino,id_guia,duracion_dias,cupo_maximo,precio,dificultad,incluye,estado) VALUES(?,?,?,?,?,?,?,?,?)",
                e.getNombre(),
                e.getDestino().getIdDestino(),
                e.getGuia().getIdGuia(),
                e.getDuracionDias(),
                e.getCupoMaximo(),
                e.getPrecio(),
                e.getDificultad().name(),
                e.getIncluye(),
                e.getEstado().name());
        e.setIdPaquete(id);
        return e;
    }

    @Override
    protected Paquete updateRow(Connection conn, Paquete e) throws SQLException {
        executeUpdate(conn,
                "UPDATE paquete SET nombre=?,id_destino=?,id_guia=?,duracion_dias=?,cupo_maximo=?,precio=?,dificultad=?,incluye=?,estado=? WHERE id_paquete=?",
                e.getNombre(),
                e.getDestino().getIdDestino(),
                e.getGuia().getIdGuia(),
                e.getDuracionDias(),
                e.getCupoMaximo(),
                e.getPrecio(),
                e.getDificultad().name(),
                e.getIncluye(),
                e.getEstado().name(),
                e.getIdPaquete());
        return e;
    }

    public List<Paquete> listarActivos() {
        return executeQuery(baseSelect() + " WHERE paquete.estado = ? ORDER BY paquete.nombre", "ACTIVO");
    }
}
