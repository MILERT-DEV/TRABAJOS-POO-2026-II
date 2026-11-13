package pe.edu.upeu.systurismo.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import pe.edu.upeu.systurismo.components.ColumnInfo;
import pe.edu.upeu.systurismo.dto.PersonaDto;
import pe.edu.upeu.systurismo.enums.TipoDocumento;
import pe.edu.upeu.systurismo.model.Turista;
import pe.edu.upeu.systurismo.service.ITuristaService;
import pe.edu.upeu.systurismo.utils.DniAutocompletador;
import pe.edu.upeu.systurismo.utils.FxUtil;

public class TuristaController extends CrudBaseController<Turista, Long> {

    @FXML private ComboBox<TipoDocumento> cbxTipoDocumento;
    @FXML private TextField txtNroDocumento;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtNacionalidad;

    private DniAutocompletador autocompletadorDni;

    public TuristaController(ITuristaService servicio) {
        super(servicio);
    }

    @Override
    protected String nombreEntidad() {
        return "turista";
    }

    @Override
    protected LinkedHashMap<String, ColumnInfo> columnas() {
        LinkedHashMap<String, ColumnInfo> columnas = new LinkedHashMap<>();
        columnas.put("ID", new ColumnInfo("idTurista", 60.0));
        columnas.put("Documento", new ColumnInfo("nroDocumento", 110.0));
        columnas.put("Nombres", new ColumnInfo("nombres", 140.0));
        columnas.put("Apellidos", new ColumnInfo("apellidos", 140.0));
        columnas.put("Correo", new ColumnInfo("email", 190.0));
        columnas.put("Teléfono", new ColumnInfo("telefono", 100.0));
        columnas.put("Nacionalidad", new ColumnInfo("nacionalidad", 110.0));
        return columnas;
    }

    @Override
    protected void prepararFormulario() {
        cbxTipoDocumento.getItems().setAll(TipoDocumento.values());
        FxUtil.soloAlfanumerico(txtNroDocumento, 15);
        FxUtil.limitarLongitud(txtNombres, 60);
        FxUtil.limitarLongitud(txtApellidos, 60);
        FxUtil.limitarLongitud(txtEmail, 80);
        FxUtil.soloDigitos(txtTelefono, 15);
        FxUtil.limitarLongitud(txtNacionalidad, 40);

        // Autocompleta nombres y apellidos al escribir un DNI de 8 dígitos
        autocompletadorDni = new DniAutocompletador(
                txtNroDocumento,
                () -> cbxTipoDocumento.getValue() == TipoDocumento.DNI,
                this::completarDesdeDni,
                this::mostrarMensaje);
        cbxTipoDocumento.valueProperty().addListener((obs, ant, nuevo) -> autocompletadorDni.buscar());
    }

    private void completarDesdeDni(PersonaDto persona) {
        txtNombres.setText(persona.getNombre());
        txtApellidos.setText(persona.getApellidos());
        if (txtNacionalidad.getText() == null || txtNacionalidad.getText().isBlank()) {
            txtNacionalidad.setText("Peruana");
        }
        txtEmail.requestFocus(); // lo siguiente que falta escribir
    }

    @Override
    protected Map<String, Control> camposValidables() {
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("tipoDocumento", cbxTipoDocumento);
        campos.put("nroDocumento", txtNroDocumento);
        campos.put("nombres", txtNombres);
        campos.put("apellidos", txtApellidos);
        campos.put("email", txtEmail);
        campos.put("telefono", txtTelefono);
        campos.put("nacionalidad", txtNacionalidad);
        return campos;
    }

    @Override
    protected Turista leerFormulario() {
        return Turista.builder()
                .tipoDocumento(cbxTipoDocumento.getValue())
                .nroDocumento(texto(txtNroDocumento))
                .nombres(texto(txtNombres))
                .apellidos(texto(txtApellidos))
                .email(texto(txtEmail))
                .telefono(texto(txtTelefono))
                .nacionalidad(texto(txtNacionalidad))
                .build();
    }

    @Override
    protected void cargarFormulario(Turista e) {
        autocompletadorDni.sinConsultar(() -> {
            cbxTipoDocumento.setValue(e.getTipoDocumento());
            txtNroDocumento.setText(e.getNroDocumento());
        });
        txtNombres.setText(e.getNombres());
        txtApellidos.setText(e.getApellidos());
        txtEmail.setText(e.getEmail());
        txtTelefono.setText(e.getTelefono());
        txtNacionalidad.setText(e.getNacionalidad());
    }

    @Override
    protected void limpiarCampos() {
        cbxTipoDocumento.setValue(null);
        txtNroDocumento.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtEmail.clear();
        txtTelefono.clear();
        txtNacionalidad.clear();
    }

    @Override
    protected Long idDe(Turista e) {
        return e.getIdTurista();
    }

    @Override
    protected void asignarId(Turista e, Long id) {
        e.setIdTurista(id);
    }
}
