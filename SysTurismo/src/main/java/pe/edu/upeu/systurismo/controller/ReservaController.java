package pe.edu.upeu.systurismo.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.upeu.systurismo.components.ColumnInfo;
import pe.edu.upeu.systurismo.components.PaginationBar;
import pe.edu.upeu.systurismo.components.TableViewHelper;
import pe.edu.upeu.systurismo.components.Toast;
import pe.edu.upeu.systurismo.components.ToltipCustom;
import pe.edu.upeu.systurismo.dto.SessionManager;
import pe.edu.upeu.systurismo.enums.EstadoReserva;
import pe.edu.upeu.systurismo.enums.MetodoPago;
import pe.edu.upeu.systurismo.exception.ModelNotFoundException;
import pe.edu.upeu.systurismo.exception.ReglaNegocioException;
import pe.edu.upeu.systurismo.exception.ValidacionException;
import pe.edu.upeu.systurismo.model.Paquete;
import pe.edu.upeu.systurismo.model.Reserva;
import pe.edu.upeu.systurismo.model.Turista;
import pe.edu.upeu.systurismo.model.Usuario;
import pe.edu.upeu.systurismo.repository.helper.PageResult;
import pe.edu.upeu.systurismo.service.IPaqueteService;
import pe.edu.upeu.systurismo.service.IReservaService;
import pe.edu.upeu.systurismo.service.ITuristaService;
import pe.edu.upeu.systurismo.utils.FxUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pantalla de reservas. No hereda del CRUD base porque una reserva no se edita ni se elimina:
 * se registra (con su adelanto, en una transacción), se le agregan pagos y se cancela.
 */
public class ReservaController {

    private static final Logger LOG = LoggerFactory.getLogger(ReservaController.class);

    private final IReservaService reservaService;
    private final ITuristaService turistaService;
    private final IPaqueteService paqueteService;

    @FXML private ComboBox<Turista> cbxTurista;
    @FXML private ComboBox<Paquete> cbxPaquete;
    @FXML private DatePicker dpFechaViaje;
    @FXML private TextField txtPersonas;
    @FXML private TextField txtAdelanto;
    @FXML private ComboBox<MetodoPago> cbxMetodo;
    @FXML private Label lblResumen;
    @FXML private Label lbnMsg;
    @FXML private TableView<Reserva> tableView;
    @FXML private TextField txtBuscar;
    @FXML private HBox boxPaginacion;

    private final ToltipCustom tooltips = new ToltipCustom();
    private final PaginationBar paginacion = new PaginationBar();
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(300));
    private int paginaActual = 0;

    public ReservaController(IReservaService reservaService, ITuristaService turistaService, IPaqueteService paqueteService) {
        this.reservaService = reservaService;
        this.turistaService = turistaService;
        this.paqueteService = paqueteService;
    }

    @FXML
    public void initialize() {
        FxUtil.configurarCombo(cbxTurista, t -> t.getNombreCompleto() + " · " + t.getNroDocumento());
        cbxTurista.getItems().setAll(turistaService.findAll().stream()
                .sorted(Comparator.comparing(Turista::getNombreCompleto)).toList());
        FxUtil.configurarCombo(cbxPaquete, p -> p.getNombre() + " · " + FxUtil.moneda(p.getPrecio()));
        cbxPaquete.getItems().setAll(paqueteService.listarActivos());
        cbxMetodo.getItems().setAll(MetodoPago.values());
        FxUtil.soloDigitos(txtPersonas, 2);
        FxUtil.soloDecimal(txtAdelanto, 7);

        dpFechaViaje.setEditable(false);
        dpFechaViaje.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacio) {
                super.updateItem(fecha, vacio);
                setDisable(vacio || !fecha.isAfter(LocalDate.now()));
            }
        });

        cbxPaquete.valueProperty().addListener((o, a, n) -> actualizarResumen());
        dpFechaViaje.valueProperty().addListener((o, a, n) -> actualizarResumen());
        txtPersonas.textProperty().addListener((o, a, n) -> actualizarResumen());
        camposValidables().values().forEach(c -> {
            if (c instanceof TextField t) {
                t.textProperty().addListener((o, a, n) -> tooltips.limpiarCampo(c));
            } else if (c instanceof javafx.scene.control.ComboBoxBase<?> cb) {
                cb.valueProperty().addListener((o, a, n) -> tooltips.limpiarCampo(c));
            }
        });

        configurarTabla();
        boxPaginacion.getChildren().add(paginacion);
        HBox.setHgrow(paginacion, Priority.ALWAYS);
        paginacion.setOnCambio((pagina, tamano) -> listar(pagina));
        esperaBusqueda.setOnFinished(e -> listar(0));
        txtBuscar.textProperty().addListener((o, a, n) -> esperaBusqueda.playFromStart());

        actualizarResumen();
        listar(0);
    }

    // ------------------------------------------------------------------- tabla

    private void configurarTabla() {
        LinkedHashMap<String, ColumnInfo> columnas = new LinkedHashMap<>();
        columnas.put("N.º", new ColumnInfo("idReserva", 55.0));
        columnas.put("Turista", new ColumnInfo("turista.nombreCompleto", 170.0));
        columnas.put("Paquete", new ColumnInfo("paquete.nombre", 170.0));
        columnas.put("Viaje", new ColumnInfo("fechaViaje", 90.0));
        columnas.put("Pers.", new ColumnInfo("numPersonas", 55.0));
        columnas.put("Total", new ColumnInfo("precioTotal", 90.0));
        columnas.put("Pagado", new ColumnInfo("pagado", 90.0));
        columnas.put("Saldo", new ColumnInfo("saldo", 90.0));
        columnas.put("Estado", new ColumnInfo("estado", 100.0));
        TableViewHelper<Reserva> helper = new TableViewHelper<>();
        helper.addColumns(tableView, columnas);

        TableColumn<Reserva, Void> acciones = new TableColumn<>("Acciones");
        acciones.setSortable(false);
        acciones.setPrefWidth(150);
        acciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnPagar = new Button("Pagar");
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox caja = new HBox(6, btnPagar, btnCancelar);

            {
                caja.setAlignment(Pos.CENTER_LEFT);
                btnPagar.getStyleClass().addAll("btn-mini", "btn-mini-ok");
                btnCancelar.getStyleClass().addAll("btn-mini", "btn-mini-peligro");
                btnPagar.setOnAction(e -> pagar(getTableView().getItems().get(getIndex())));
                btnCancelar.setOnAction(e -> cancelar(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio) {
                    setGraphic(null);
                    return;
                }
                Reserva r = getTableView().getItems().get(getIndex());
                boolean cancelada = r.getEstado() == EstadoReserva.CANCELADA;
                btnPagar.setDisable(cancelada || r.getSaldo().signum() <= 0);
                btnCancelar.setDisable(cancelada);
                setGraphic(caja);
            }
        });
        tableView.getColumns().add(acciones);
        tableView.setPlaceholder(new Label("No hay reservas para mostrar"));
    }

    private void listar(int pagina) {
        try {
            PageResult<Reserva> r = reservaService.findPage(txtBuscar.getText(), pagina, paginacion.getTamano());
            if (r.items().isEmpty() && pagina > 0) {
                listar(pagina - 1);
                return;
            }
            tableView.getItems().setAll(r.items());
            paginacion.actualizar(r);
            paginaActual = r.page();
        } catch (RuntimeException e) {
            LOG.error("No se pudo cargar las reservas", e);
            mostrarMensaje("No se pudo cargar la lista: " + e.getMessage(), true);
        }
    }

    // ------------------------------------------------------------- formulario

    private Map<String, Control> camposValidables() {
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("turista", cbxTurista);
        campos.put("paquete", cbxPaquete);
        campos.put("fechaViaje", dpFechaViaje);
        campos.put("numPersonas", txtPersonas);
        campos.put("adelanto", txtAdelanto);
        campos.put("metodoPago", cbxMetodo);
        return campos;
    }

    private void actualizarResumen() {
        Paquete paquete = cbxPaquete.getValue();
        if (paquete == null) {
            lblResumen.setText("Elija un paquete para ver el total y los cupos.");
            return;
        }
        int personas = 0;
        try {
            personas = txtPersonas.getText() == null || txtPersonas.getText().isBlank() ? 0 : Integer.parseInt(txtPersonas.getText());
        } catch (NumberFormatException ignorado) {
            // el campo solo admite dígitos; no debería ocurrir
        }
        BigDecimal total = reservaService.calcularTotal(paquete, personas);
        StringBuilder sb = new StringBuilder();
        sb.append("Precio por persona: ").append(FxUtil.moneda(paquete.getPrecio())).append('\n');
        sb.append("Total a pagar: ").append(FxUtil.moneda(total)).append('\n');
        sb.append("Adelanto para confirmar (30%): ").append(FxUtil.moneda(reservaService.adelantoMinimo(total)));
        if (dpFechaViaje.getValue() != null) {
            int libres = reservaService.cuposDisponibles(paquete, dpFechaViaje.getValue());
            sb.append('\n').append("Cupos libres ese día: ").append(libres).append(" de ").append(paquete.getCupoMaximo());
        }
        lblResumen.setText(sb.toString());
    }

    @FXML
    public void reservar() {
        limpiarErrores();
        Map<String, String> erroresLectura = new LinkedHashMap<>();
        Integer personas = null;
        if (txtPersonas.getText() != null && !txtPersonas.getText().isBlank()) {
            personas = Integer.valueOf(txtPersonas.getText());
        }
        BigDecimal adelanto = BigDecimal.ZERO;
        if (txtAdelanto.getText() != null && !txtAdelanto.getText().isBlank()) {
            try {
                adelanto = new BigDecimal(txtAdelanto.getText());
            } catch (NumberFormatException e) {
                erroresLectura.put("adelanto", "Ingrese un monto válido");
            }
        }
        if (!erroresLectura.isEmpty()) {
            mostrarErrores(erroresLectura);
            return;
        }

        Reserva reserva = Reserva.builder()
                .turista(cbxTurista.getValue())
                .paquete(cbxPaquete.getValue())
                .fechaViaje(dpFechaViaje.getValue())
                .numPersonas(personas)
                .usuario(Usuario.builder().idUsuario(SessionManager.getInstance().getUserId()).build())
                .build();
        try {
            Reserva guardada = reservaService.reservar(reserva, adelanto, cbxMetodo.getValue());
            Toast.exito(stage(), "Reserva registrada · " + FxUtil.moneda(guardada.getPrecioTotal())
                    + " · " + guardada.getEstado().getDescripcion());
            limpiarFormulario();
            listar(0);
        } catch (ValidacionException e) {
            mostrarErrores(e.getErrores());
        } catch (ReglaNegocioException | ModelNotFoundException e) {
            mostrarMensaje(e.getMessage(), true);
            Toast.error(stage(), e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("Error al reservar", e);
            mostrarMensaje("Ocurrió un error inesperado. Intente de nuevo.", true);
        }
    }

    @FXML
    public void limpiarFormulario() {
        limpiarErrores();
        cbxTurista.setValue(null);
        cbxPaquete.setValue(null);
        dpFechaViaje.setValue(null);
        txtPersonas.clear();
        txtAdelanto.clear();
        cbxMetodo.setValue(null);
        lbnMsg.setText("");
        actualizarResumen();
    }

    // ----------------------------------------------------- pagos y cancelaciones

    private void pagar(Reserva reserva) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Registrar pago");
        dialogo.setHeaderText("Reserva N.º " + reserva.getIdReserva() + " · saldo " + FxUtil.moneda(reserva.getSaldo()));
        TextField txtMonto = new TextField(reserva.getSaldo().toPlainString());
        FxUtil.soloDecimal(txtMonto, 7);
        ComboBox<MetodoPago> cbxMetodoPago = new ComboBox<>(FXCollections.observableArrayList(MetodoPago.values()));
        cbxMetodoPago.setValue(MetodoPago.EFECTIVO);
        cbxMetodoPago.setMaxWidth(Double.MAX_VALUE);

        GridPane grilla = new GridPane();
        grilla.setHgap(12);
        grilla.setVgap(12);
        grilla.addRow(0, new Label("Monto (S/)"), txtMonto);
        grilla.addRow(1, new Label("Método de pago"), cbxMetodoPago);
        dialogo.getDialogPane().setContent(grilla);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogo.getDialogPane().getStylesheets().add(FxUtil.cssTurismo());
        dialogo.initOwner(stage());
        Platform.runLater(txtMonto::requestFocus);

        if (dialogo.showAndWait().filter(b -> b == ButtonType.OK).isEmpty()) {
            return;
        }
        try {
            BigDecimal monto = txtMonto.getText().isBlank() ? null : new BigDecimal(txtMonto.getText());
            Reserva actualizada = reservaService.registrarPago(reserva.getIdReserva(), monto, cbxMetodoPago.getValue());
            Toast.exito(stage(), "Pago registrado · reserva " + actualizada.getEstado().getDescripcion().toLowerCase());
            listar(paginaActual);
        } catch (NumberFormatException e) {
            Toast.error(stage(), "Ingrese un monto válido");
        } catch (ReglaNegocioException | ModelNotFoundException e) {
            Toast.error(stage(), e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("Error al registrar el pago", e);
            Toast.error(stage(), "No se pudo registrar el pago");
        }
    }

    private void cancelar(Reserva reserva) {
        boolean acepto = FxUtil.confirmar(stage(), "Cancelar reserva",
                "¿Cancelar la reserva N.º " + reserva.getIdReserva() + "? Los cupos quedarán libres otra vez.");
        if (!acepto) {
            return;
        }
        try {
            reservaService.cancelar(reserva.getIdReserva());
            Toast.exito(stage(), "Reserva cancelada");
            listar(paginaActual);
            actualizarResumen();
        } catch (ReglaNegocioException | ModelNotFoundException e) {
            Toast.error(stage(), e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("Error al cancelar", e);
            Toast.error(stage(), "No se pudo cancelar la reserva");
        }
    }

    // --------------------------------------------------------------- mensajes

    private void mostrarErrores(Map<String, String> errores) {
        limpiarErrores();
        Control primero = null;
        String primerMensaje = null;
        for (Map.Entry<String, Control> campo : camposValidables().entrySet()) {
            String mensaje = errores.get(campo.getKey());
            if (mensaje != null) {
                tooltips.marcarError(campo.getValue(), mensaje);
                if (primero == null) {
                    primero = campo.getValue();
                    primerMensaje = mensaje;
                }
            }
        }
        if (primerMensaje == null && !errores.isEmpty()) {
            primerMensaje = errores.values().iterator().next();
        }
        if (primerMensaje != null) {
            mostrarMensaje(primerMensaje, true);
        }
        Toast.aviso(stage(), "Revise los campos marcados en rojo");
        if (primero != null) {
            Control foco = primero;
            Platform.runLater(foco::requestFocus);
        }
    }

    private void limpiarErrores() {
        camposValidables().values().forEach(tooltips::limpiarCampo);
        lbnMsg.setText("");
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lbnMsg.setText(mensaje);
        lbnMsg.getStyleClass().removeAll("msg-error", "msg-ok");
        lbnMsg.getStyleClass().add(esError ? "msg-error" : "msg-ok");
    }

    private Stage stage() {
        return (Stage) tableView.getScene().getWindow();
    }
}
