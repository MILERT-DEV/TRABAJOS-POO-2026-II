package pe.edu.upeu.systurismo.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import pe.edu.upeu.systurismo.components.ColumnInfo;
import pe.edu.upeu.systurismo.dto.PersonaDto;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.model.Guia;
import pe.edu.upeu.systurismo.service.IGuiaService;
import pe.edu.upeu.systurismo.utils.DniAutocompletador;
import pe.edu.upeu.systurismo.utils.FxUtil;

public class GuiaController extends CrudBaseController<Guia, Long> {

    @FXML private TextField txtNombres;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtIdiomas;
    @FXML private ComboBox<EstadoRegistro> cbxEstado;

    private DniAutocompletador autocompletadorDni;

    public GuiaController(IGuiaService servicio) {
        super(servicio);
    }

    @Override
    protected String nombreEntidad() {
        return "guía";
    }

    @Override
    protected LinkedHashMap<String, ColumnInfo> columnas() {
        LinkedHashMap<String, ColumnInfo> columnas = new LinkedHashMap<>();
        columnas.put("ID", new ColumnInfo("idGuia", 60.0));
        columnas.put("Nombres", new ColumnInfo("nombres", 200.0));
        columnas.put("DNI", new ColumnInfo("dni", 90.0));
        columnas.put("Celular", new ColumnInfo("telefono", 100.0));
        columnas.put("Idiomas", new ColumnInfo("idiomas", 150.0));
        columnas.put("Estado", new ColumnInfo("estado", 100.0));
        return columnas;
    }

    @Override
    protected void prepararFormulario() {
        FxUtil.limitarLongitud(txtNombres, 80);
        FxUtil.soloDigitos(txtDni, 8);
        FxUtil.soloDigitos(txtTelefono, 9);
        FxUtil.limitarLongitud(txtEmail, 80);
        FxUtil.limitarLongitud(txtIdiomas, 100);
        cbxEstado.getItems().setAll(EstadoRegistro.values());

        // Al escribir el DNI se completa el nombre del guía
        autocompletadorDni = new DniAutocompletador(
                txtDni,
                () -> true,
                this::completarDesdeDni,
                this::mostrarMensaje);
    }

    private void completarDesdeDni(PersonaDto persona) {
        String completo = (persona.getNombre() + " " + persona.getApellidos()).trim();
        txtNombres.setText(completo.length() > 80 ? completo.substring(0, 80) : completo);
        txtTelefono.requestFocus();
    }

    @Override
    protected Map<String, Control> camposValidables() {
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombres", txtNombres);
        campos.put("dni", txtDni);
        campos.put("telefono", txtTelefono);
        campos.put("email", txtEmail);
        campos.put("idiomas", txtIdiomas);
        campos.put("estado", cbxEstado);
        return campos;
    }

    @Override
    protected Guia leerFormulario() {
        return Guia.builder()
                .nombres(texto(txtNombres))
                .dni(texto(txtDni))
                .telefono(texto(txtTelefono))
                .email(texto(txtEmail))
                .idiomas(texto(txtIdiomas))
                .estado(cbxEstado.getValue())
                .build();
    }

    @Override
    protected void cargarFormulario(Guia e) {
        txtNombres.setText(e.getNombres());
        autocompletadorDni.sinConsultar(() -> txtDni.setText(e.getDni()));
        txtTelefono.setText(e.getTelefono());
        txtEmail.setText(e.getEmail());
        txtIdiomas.setText(e.getIdiomas());
        cbxEstado.setValue(e.getEstado());
    }

    @Override
    protected void limpiarCampos() {
        txtNombres.clear();
        txtDni.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtIdiomas.clear();
        cbxEstado.setValue(EstadoRegistro.ACTIVO);
    }

    @Override
    protected Long idDe(Guia e) {
        return e.getIdGuia();
    }

    @Override
    protected void asignarId(Guia e, Long id) {
        e.setIdGuia(id);
    }
}
