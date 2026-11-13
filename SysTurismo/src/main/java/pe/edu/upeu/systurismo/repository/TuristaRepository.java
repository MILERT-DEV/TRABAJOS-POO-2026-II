package pe.edu.upeu.systurismo.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.systurismo.enums.TipoDocumento;
import pe.edu.upeu.systurismo.model.Turista;

public class TuristaRepository extends AbstractJpaRepository<Turista, Long> {

    @Override
    protected String getTableName() {
        return "turista";
    }

    @Override
    protected String getPkColumn() {
        return "id_turista";
    }

    @Override
    protected List<String> searchColumns() {
        return List.of("turista.nombres", "turista.apellidos", "turista.nro_documento", "turista.email", "turista.nacionalidad");
    }

    @Override
    protected Turista mapRow(ResultSet rs) throws SQLException {
        return Turista.builder()
                .idTurista(rs.getLong("id_turista"))
                .tipoDocumento(TipoDocumento.valueOf(rs.getString("tipo_documento")))
                .nroDocumento(rs.getString("nro_documento"))
                .nombres(rs.getString("nombres"))
                .apellidos(rs.getString("apellidos"))
                .email(rs.getString("email"))
                .telefono(rs.getString("telefono"))
                .nacionalidad(rs.getString("nacionalidad"))
                .build();
    }

    @Override
    protected Turista insert(Connection conn, Turista e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO turista(tipo_documento,nro_documento,nombres,apellidos,email,telefono,nacionalidad) VALUES(?,?,?,?,?,?,?)",
                e.getTipoDocumento().name(),
                e.getNroDocumento(),
                e.getNombres(),
                e.getApellidos(),
                e.getEmail(),
                e.getTelefono(),
                e.getNacionalidad());
        e.setIdTurista(id);
        return e;
    }

    @Override
    protected Turista updateRow(Connection conn, Turista e) throws SQLException {
        executeUpdate(conn,
                "UPDATE turista SET tipo_documento=?,nro_documento=?,nombres=?,apellidos=?,email=?,telefono=?,nacionalidad=? WHERE id_turista=?",
                e.getTipoDocumento().name(),
                e.getNroDocumento(),
                e.getNombres(),
                e.getApellidos(),
                e.getEmail(),
                e.getTelefono(),
                e.getNacionalidad(),
                e.getIdTurista());
        return e;
    }

    public Optional<Turista> buscarPorDocumento(String nroDocumento) {
        return executeQueryOne(baseSelect() + " WHERE turista.nro_documento = ?", nroDocumento);
    }
}
