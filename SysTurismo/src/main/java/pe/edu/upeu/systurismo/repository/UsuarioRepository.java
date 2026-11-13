package pe.edu.upeu.systurismo.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.model.Perfil;
import pe.edu.upeu.systurismo.model.Usuario;

public class UsuarioRepository extends AbstractJpaRepository<Usuario, Long> {

    @Override
    protected String getTableName() {
        return "usuario";
    }

    @Override
    protected String getPkColumn() {
        return "id_usuario";
    }

    @Override
    protected List<String> searchColumns() {
        return List.of("usuario.usuario", "usuario.nombre_completo");
    }

    @Override
    protected String selectColumns() {
        return "usuario.*, perfil.nombre AS perfil_nombre, perfil.codigo AS perfil_codigo";
    }

    @Override
    protected String fromClause() {
        return "usuario JOIN perfil ON usuario.id_perfil = perfil.id_perfil";
    }

    @Override
    protected Usuario mapRow(ResultSet rs) throws SQLException {
        return Usuario.builder()
                .idUsuario(rs.getLong("id_usuario"))
                .usuario(rs.getString("usuario"))
                .nombreCompleto(rs.getString("nombre_completo"))
                .clave(rs.getString("clave"))
                .perfil(Perfil.builder().idPerfil(rs.getLong("id_perfil")).nombre(rs.getString("perfil_nombre")).codigo(rs.getString("perfil_codigo")).build())
                .estado(EstadoRegistro.valueOf(rs.getString("estado")))
                .build();
    }

    @Override
    protected Usuario insert(Connection conn, Usuario e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO usuario(usuario,nombre_completo,clave,id_perfil,estado) VALUES(?,?,?,?,?)",
                e.getUsuario(),
                e.getNombreCompleto(),
                e.getClave(),
                e.getPerfil().getIdPerfil(),
                e.getEstado().name());
        e.setIdUsuario(id);
        return e;
    }

    @Override
    protected Usuario updateRow(Connection conn, Usuario e) throws SQLException {
        executeUpdate(conn,
                "UPDATE usuario SET usuario=?,nombre_completo=?,clave=?,id_perfil=?,estado=? WHERE id_usuario=?",
                e.getUsuario(),
                e.getNombreCompleto(),
                e.getClave(),
                e.getPerfil().getIdPerfil(),
                e.getEstado().name(),
                e.getIdUsuario());
        return e;
    }

    public Optional<Usuario> buscarPorUsuario(String usuario) {
        return executeQueryOne(baseSelect() + " WHERE usuario.usuario = ?", usuario);
    }
}
