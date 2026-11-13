package pe.edu.upeu.systurismo.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import pe.edu.upeu.systurismo.model.Perfil;

public class PerfilRepository extends AbstractJpaRepository<Perfil, Long> {

    @Override
    protected String getTableName() {
        return "perfil";
    }

    @Override
    protected String getPkColumn() {
        return "id_perfil";
    }

    @Override
    protected List<String> searchColumns() {
        return List.of("perfil.nombre");
    }

    @Override
    protected String orderBy() {
        return "perfil.id_perfil ASC";
    }

    @Override
    protected Perfil mapRow(ResultSet rs) throws SQLException {
        return Perfil.builder()
                .idPerfil(rs.getLong("id_perfil"))
                .nombre(rs.getString("nombre"))
                .codigo(rs.getString("codigo"))
                .build();
    }

    @Override
    protected Perfil insert(Connection conn, Perfil e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO perfil(nombre,codigo) VALUES(?,?)",
                e.getNombre(),
                e.getCodigo());
        e.setIdPerfil(id);
        return e;
    }

    @Override
    protected Perfil updateRow(Connection conn, Perfil e) throws SQLException {
        executeUpdate(conn,
                "UPDATE perfil SET nombre=?,codigo=? WHERE id_perfil=?",
                e.getNombre(),
                e.getCodigo(),
                e.getIdPerfil());
        return e;
    }
}
