package pe.edu.upeu.systurismo.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.enums.TipoDestino;
import pe.edu.upeu.systurismo.model.Destino;

public class DestinoRepository extends AbstractJpaRepository<Destino, Long> {

    @Override
    protected String getTableName() {
        return "destino";
    }

    @Override
    protected String getPkColumn() {
        return "id_destino";
    }

    @Override
    protected List<String> searchColumns() {
        return List.of("destino.nombre", "destino.pais", "destino.ciudad");
    }

    @Override
    protected Destino mapRow(ResultSet rs) throws SQLException {
        return Destino.builder()
                .idDestino(rs.getLong("id_destino"))
                .nombre(rs.getString("nombre"))
                .pais(rs.getString("pais"))
                .ciudad(rs.getString("ciudad"))
                .tipoDestino(TipoDestino.valueOf(rs.getString("tipo_destino")))
                .descripcion(rs.getString("descripcion"))
                .estado(EstadoRegistro.valueOf(rs.getString("estado")))
                .build();
    }

    @Override
    protected Destino insert(Connection conn, Destino e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO destino(nombre,pais,ciudad,tipo_destino,descripcion,estado) VALUES(?,?,?,?,?,?)",
                e.getNombre(),
                e.getPais(),
                e.getCiudad(),
                e.getTipoDestino().name(),
                e.getDescripcion(),
                e.getEstado().name());
        e.setIdDestino(id);
        return e;
    }

    @Override
    protected Destino updateRow(Connection conn, Destino e) throws SQLException {
        executeUpdate(conn,
                "UPDATE destino SET nombre=?,pais=?,ciudad=?,tipo_destino=?,descripcion=?,estado=? WHERE id_destino=?",
                e.getNombre(),
                e.getPais(),
                e.getCiudad(),
                e.getTipoDestino().name(),
                e.getDescripcion(),
                e.getEstado().name(),
                e.getIdDestino());
        return e;
    }
}
