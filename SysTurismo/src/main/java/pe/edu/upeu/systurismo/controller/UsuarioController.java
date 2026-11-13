package pe.edu.upeu.systurismo.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pe.edu.upeu.systurismo.components.ColumnInfo;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.model.Perfil;
import pe.edu.upeu.systurismo.model.Usuario;
import pe.edu.upeu.systurismo.service.IPerfilService;
import pe.edu.upeu.systurismo.service.IUsuarioService;
import pe.edu.upeu.systurismo.utils.FxUtil;

public class UsuarioController extends CrudBaseController<Usuario, Long> {

    @FXML private TextField txtUsuario;
    @FXML private TextField txtNombreCompleto;
    @FXML private PasswordField txtClave;
    @FXML private ComboBox<Perfil> cbxPerfil;
    @FXML private ComboBox<EstadoRegistro> cbxEstado;
    private final IPerfilService perfilService;

    public UsuarioController(IUsuarioService servicio, IPerfilService perfilService) {
        super(servicio);
        this.perfilService = perfilService;
    }

    @Override
    protected String nombreEntidad() {
        return "usuario";
    }

    @Override
    protected LinkedHashMap<String, ColumnInfo> columnas() {
        LinkedHashMap<String, ColumnInfo> columnas = new LinkedHashMap<>();
        columnas.put("ID", new ColumnInfo("idUsuario", 60.0));
        columnas.put("Usuario", new ColumnInfo("usuario", 120.0));
        columnas.put("Nombre completo", new ColumnInfo("nombreCompleto", 220.0));
        columnas.put("Perfil", new ColumnInfo("perfil.nombre", 130.0));
        columnas.put("Estado", new ColumnInfo("estado", 100.0));
        return columnas;
    }

    @Override
    protected void prepararFormulario() {
        FxUtil.soloAlfanumerico(txtUsuario, 20);
        FxUtil.limitarLongitud(txtNombreCompleto, 80);
        FxUtil.limitarLongitud(txtClave, 40);
        FxUtil.configurarCombo(cbxPerfil, p -> p.getNombre());
        cbxPerfil.getItems().setAll(perfilService.findAll());
        cbxEstado.getItems().setAll(EstadoRegistro.values());
        txtClave.setPromptText("Mínimo 8 caracteres (vacío al editar = no cambiar)");
    }

    @Override
    protected Map<String, Control> camposValidables() {
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("usuario", txtUsuario);
        campos.put("nombreCompleto", txtNombreCompleto);
        campos.put("clave", txtClave);
        campos.put("perfil", cbxPerfil);
        campos.put("estado", cbxEstado);
        return campos;
    }

    @Override
    protected Usuario leerFormulario() {
        return Usuario.builder()
                .usuario(texto(txtUsuario))
                .nombreCompleto(texto(txtNombreCompleto))
                .clave(txtClave.getText() == null ? "" : txtClave.getText())
                .perfil(cbxPerfil.getValue())
                .estado(cbxEstado.getValue())
                .build();
    }

    @Override
    protected void cargarFormulario(Usuario e) {
        txtUsuario.setText(e.getUsuario());
        txtNombreCompleto.setText(e.getNombreCompleto());
        txtClave.clear();
        FxUtil.seleccionarPorId(cbxPerfil, e.getPerfil() == null ? null : e.getPerfil().getIdPerfil(), Perfil::getIdPerfil);
        cbxEstado.setValue(e.getEstado());
    }

    @Override
    protected void limpiarCampos() {
        txtUsuario.clear();
        txtNombreCompleto.clear();
        txtClave.clear();
        cbxPerfil.setValue(null);
        cbxEstado.setValue(EstadoRegistro.ACTIVO);
    }

    @Override
    protected Long idDe(Usuario e) {
        return e.getIdUsuario();
    }

    @Override
    protected void asignarId(Usuario e, Long id) {
        e.setIdUsuario(id);
    }
}
