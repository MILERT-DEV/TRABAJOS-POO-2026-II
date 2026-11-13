package pe.edu.upeu.systurismo.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.model.Guia;

public class GuiaRepository extends AbstractJpaRepository<Guia, Long> {

    @Override
    protected String getTableName() {
        return "guia";
    }

    @Override
    protected String getPkColumn() {
        return "id_guia";
    }

    @Override
    protected List<String> searchColumns() {
        return List.of("guia.nombres", "guia.dni", "guia.idiomas", "guia.email");
    }

    @Override
    protected Guia mapRow(ResultSet rs) throws SQLException {
        return Guia.builder()
                .idGuia(rs.getLong("id_guia"))
                .nombres(rs.getString("nombres"))
                .dni(rs.getString("dni"))
                .telefono(rs.getString("telefono"))
                .email(rs.getString("email"))
                .idiomas(rs.getString("idiomas"))
                .estado(EstadoRegistro.valueOf(rs.getString("estado")))
                .build();
    }

    @Override
    protected Guia insert(Connection conn, Guia e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO guia(nombres,dni,telefono,email,idiomas,estado) VALUES(?,?,?,?,?,?)",
                e.getNombres(),
                e.getDni(),
                e.getTelefono(),
                e.getEmail(),
                e.getIdiomas(),
                e.getEstado().name());
        e.setIdGuia(id);
        return e;
    }

    @Override
    protected Guia updateRow(Connection conn, Guia e) throws SQLException {
        executeUpdate(conn,
                "UPDATE guia SET nombres=?,dni=?,telefono=?,email=?,idiomas=?,estado=? WHERE id_guia=?",
                e.getNombres(),
                e.getDni(),
                e.getTelefono(),
                e.getEmail(),
                e.getIdiomas(),
                e.getEstado().name(),
                e.getIdGuia());
        return e;
    }

    public Optional<Guia> buscarPorDni(String dni) {
        return executeQueryOne(baseSelect() + " WHERE guia.dni = ?", dni);
    }
}
