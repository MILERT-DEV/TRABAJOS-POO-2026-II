package pe.edu.upeu.systurismo.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pe.edu.upeu.systurismo.components.ColumnInfo;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.enums.TipoDestino;
import pe.edu.upeu.systurismo.model.Destino;
import pe.edu.upeu.systurismo.service.IDestinoService;
import pe.edu.upeu.systurismo.utils.FxUtil;

public class DestinoController extends CrudBaseController<Destino, Long> {

    @FXML private TextField txtNombre;
    @FXML private TextField txtPais;
    @FXML private TextField txtCiudad;
    @FXML private ComboBox<TipoDestino> cbxTipoDestino;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<EstadoRegistro> cbxEstado;

    public DestinoController(IDestinoService servicio) {
        super(servicio);
    }

    @Override
    protected String nombreEntidad() {
        return "destino";
    }

    @Override
    protected LinkedHashMap<String, ColumnInfo> columnas() {
        LinkedHashMap<String, ColumnInfo> columnas = new LinkedHashMap<>();
        columnas.put("ID", new ColumnInfo("idDestino", 60.0));
        columnas.put("Destino", new ColumnInfo("nombre", 190.0));
        columnas.put("País", new ColumnInfo("pais", 100.0));
        columnas.put("Ciudad", new ColumnInfo("ciudad", 130.0));
        columnas.put("Tipo", new ColumnInfo("tipoDestino", 110.0));
        columnas.put("Estado", new ColumnInfo("estado", 100.0));
        return columnas;
    }

    @Override
    protected void prepararFormulario() {
        FxUtil.limitarLongitud(txtNombre, 80);
        FxUtil.limitarLongitud(txtPais, 40);
        FxUtil.limitarLongitud(txtCiudad, 60);
        cbxTipoDestino.getItems().setAll(TipoDestino.values());
        FxUtil.limitarLongitud(txtDescripcion, 300);
        cbxEstado.getItems().setAll(EstadoRegistro.values());
    }

    @Override
    protected Map<String, Control> camposValidables() {
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombre);
        campos.put("pais", txtPais);
        campos.put("ciudad", txtCiudad);
        campos.put("tipoDestino", cbxTipoDestino);
        campos.put("descripcion", txtDescripcion);
        campos.put("estado", cbxEstado);
        return campos;
    }

    @Override
    protected Destino leerFormulario() {
        return Destino.builder()
                .nombre(texto(txtNombre))
                .pais(texto(txtPais))
                .ciudad(texto(txtCiudad))
                .tipoDestino(cbxTipoDestino.getValue())
                .descripcion(texto(txtDescripcion))
                .estado(cbxEstado.getValue())
                .build();
    }

    @Override
    protected void cargarFormulario(Destino e) {
        txtNombre.setText(e.getNombre());
        txtPais.setText(e.getPais());
        txtCiudad.setText(e.getCiudad());
        cbxTipoDestino.setValue(e.getTipoDestino());
        txtDescripcion.setText(e.getDescripcion());
        cbxEstado.setValue(e.getEstado());
    }

    @Override
    protected void limpiarCampos() {
        txtNombre.clear();
        txtPais.clear();
        txtCiudad.clear();
        cbxTipoDestino.setValue(null);
        txtDescripcion.clear();
        cbxEstado.setValue(EstadoRegistro.ACTIVO);
    }

    @Override
    protected Long idDe(Destino e) {
        return e.getIdDestino();
    }

    @Override
    protected void asignarId(Destino e, Long id) {
        e.setIdDestino(id);
    }
}
