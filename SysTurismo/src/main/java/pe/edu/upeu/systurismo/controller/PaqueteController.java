package pe.edu.upeu.systurismo.controller;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pe.edu.upeu.systurismo.components.ColumnInfo;
import pe.edu.upeu.systurismo.enums.Dificultad;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.model.Destino;
import pe.edu.upeu.systurismo.model.Guia;
import pe.edu.upeu.systurismo.model.Paquete;
import pe.edu.upeu.systurismo.service.IDestinoService;
import pe.edu.upeu.systurismo.service.IGuiaService;
import pe.edu.upeu.systurismo.service.IPaqueteService;
import pe.edu.upeu.systurismo.utils.FxUtil;

public class PaqueteController extends CrudBaseController<Paquete, Long> {

    @FXML private TextField txtNombre;
    @FXML private ComboBox<Destino> cbxDestino;
    @FXML private ComboBox<Guia> cbxGuia;
    @FXML private TextField txtDuracionDias;
    @FXML private TextField txtCupoMaximo;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<Dificultad> cbxDificultad;
    @FXML private TextArea txtIncluye;
    @FXML private ComboBox<EstadoRegistro> cbxEstado;
    private final IDestinoService destinoService;
    private final IGuiaService guiaService;

    public PaqueteController(IPaqueteService servicio, IDestinoService destinoService, IGuiaService guiaService) {
        super(servicio);
        this.destinoService = destinoService;
        this.guiaService = guiaService;
    }

    @Override
    protected String nombreEntidad() {
        return "paquete";
    }

    @Override
    protected LinkedHashMap<String, ColumnInfo> columnas() {
        LinkedHashMap<String, ColumnInfo> columnas = new LinkedHashMap<>();
        columnas.put("ID", new ColumnInfo("idPaquete", 60.0));
        columnas.put("Paquete", new ColumnInfo("nombre", 200.0));
        columnas.put("Destino", new ColumnInfo("destino.nombre", 150.0));
        columnas.put("Días", new ColumnInfo("duracionDias", 60.0));
        columnas.put("Cupo", new ColumnInfo("cupoMaximo", 60.0));
        columnas.put("Precio (S/)", new ColumnInfo("precio", 100.0));
        columnas.put("Estado", new ColumnInfo("estado", 100.0));
        return columnas;
    }

    @Override
    protected void prepararFormulario() {
        FxUtil.limitarLongitud(txtNombre, 80);
        FxUtil.configurarCombo(cbxDestino, d -> d.getNombre() + " · " + d.getCiudad());
        cbxDestino.getItems().setAll(destinoService.findAll());
        FxUtil.configurarCombo(cbxGuia, g -> g.getNombres());
        cbxGuia.getItems().setAll(guiaService.findAll());
        FxUtil.soloDigitos(txtDuracionDias, 2);
        FxUtil.soloDigitos(txtCupoMaximo, 2);
        FxUtil.soloDecimal(txtPrecio, 7);
        cbxDificultad.getItems().setAll(Dificultad.values());
        FxUtil.limitarLongitud(txtIncluye, 300);
        cbxEstado.getItems().setAll(EstadoRegistro.values());
    }

    @Override
    protected Map<String, Control> camposValidables() {
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombre);
        campos.put("destino", cbxDestino);
        campos.put("guia", cbxGuia);
        campos.put("duracionDias", txtDuracionDias);
        campos.put("cupoMaximo", txtCupoMaximo);
        campos.put("precio", txtPrecio);
        campos.put("dificultad", cbxDificultad);
        campos.put("incluye", txtIncluye);
        campos.put("estado", cbxEstado);
        return campos;
    }

    @Override
    protected Paquete leerFormulario() {
        return Paquete.builder()
                .nombre(texto(txtNombre))
                .destino(cbxDestino.getValue())
                .guia(cbxGuia.getValue())
                .duracionDias(entero(txtDuracionDias, "duracionDias"))
                .cupoMaximo(entero(txtCupoMaximo, "cupoMaximo"))
                .precio(decimal(txtPrecio, "precio"))
                .dificultad(cbxDificultad.getValue())
                .incluye(texto(txtIncluye))
                .estado(cbxEstado.getValue())
                .build();
    }

    @Override
    protected void cargarFormulario(Paquete e) {
        txtNombre.setText(e.getNombre());
        FxUtil.seleccionarPorId(cbxDestino, e.getDestino() == null ? null : e.getDestino().getIdDestino(), Destino::getIdDestino);
        FxUtil.seleccionarPorId(cbxGuia, e.getGuia() == null ? null : e.getGuia().getIdGuia(), Guia::getIdGuia);
        txtDuracionDias.setText(e.getDuracionDias() == null ? "" : String.valueOf(e.getDuracionDias()));
        txtCupoMaximo.setText(e.getCupoMaximo() == null ? "" : String.valueOf(e.getCupoMaximo()));
        txtPrecio.setText(e.getPrecio() == null ? "" : e.getPrecio().toPlainString());
        cbxDificultad.setValue(e.getDificultad());
        txtIncluye.setText(e.getIncluye());
        cbxEstado.setValue(e.getEstado());
    }

    @Override
    protected void limpiarCampos() {
        txtNombre.clear();
        cbxDestino.setValue(null);
        cbxGuia.setValue(null);
        txtDuracionDias.clear();
        txtCupoMaximo.clear();
        txtPrecio.clear();
        cbxDificultad.setValue(null);
        txtIncluye.clear();
        cbxEstado.setValue(EstadoRegistro.ACTIVO);
    }

    @Override
    protected Long idDe(Paquete e) {
        return e.getIdPaquete();
    }

    @Override
    protected void asignarId(Paquete e, Long id) {
        e.setIdPaquete(id);
    }
}
