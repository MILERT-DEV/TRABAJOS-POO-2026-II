package pe.edu.upeu.systurismo.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.upeu.systurismo.dto.ResumenDashboard;
import pe.edu.upeu.systurismo.dto.SessionManager;
import pe.edu.upeu.systurismo.service.IDashboardService;
import pe.edu.upeu.systurismo.utils.FxUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardController {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardController.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final IDashboardService dashboardService;

    @FXML private Label lblBienvenida;
    @FXML private Label lblTuristas;
    @FXML private Label lblPaquetes;
    @FXML private Label lblReservasMes;
    @FXML private Label lblIngresos;
    @FXML private PieChart chartEstados;
    @FXML private BarChart<String, Number> chartIngresos;
    @FXML private TableView<ResumenDashboard.Salida> tblSalidas;
    @FXML private TableColumn<ResumenDashboard.Salida, String> colFecha;
    @FXML private TableColumn<ResumenDashboard.Salida, String> colPaquete;
    @FXML private TableColumn<ResumenDashboard.Salida, String> colPersonas;

    public DashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @FXML
    public void initialize() {
        String nombre = SessionManager.getInstance().getNombreCompleto();
        lblBienvenida.setText("Hola, " + (nombre == null ? "bienvenido" : nombre.split(" ")[0]) + ". Este es el resumen de la agencia.");

        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fecha().format(FORMATO_FECHA)));
        colPaquete.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().paquete()));
        colPersonas.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().personas())));
        tblSalidas.setPlaceholder(new Label("No hay salidas programadas"));
        tblSalidas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        chartEstados.setAnimated(false);
        chartIngresos.setAnimated(false);
        chartIngresos.setLegendVisible(false);
        cargar();
    }

    private void cargar() {
        try {
            ResumenDashboard r = dashboardService.resumen();
            lblTuristas.setText(String.valueOf(r.getTotalTuristas()));
            lblPaquetes.setText(String.valueOf(r.getPaquetesActivos()));
            lblReservasMes.setText(String.valueOf(r.getReservasDelMes()));
            lblIngresos.setText(FxUtil.moneda(r.getIngresosCobrados()));

            // Siempre las tres series y en el mismo orden, para que cada estado conserve su color.
            Map<String, Long> estados = r.getReservasPorEstado();
            chartEstados.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Pendientes", estados.getOrDefault("PENDIENTE", 0L)),
                    new PieChart.Data("Confirmadas", estados.getOrDefault("CONFIRMADA", 0L)),
                    new PieChart.Data("Canceladas", estados.getOrDefault("CANCELADA", 0L))));

            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            for (Map.Entry<String, BigDecimal> e : r.getIngresosPorPaquete().entrySet()) {
                serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
            }
            chartIngresos.getData().setAll(java.util.List.of(serie));

            tblSalidas.getItems().setAll(r.getProximasSalidas());
        } catch (RuntimeException e) {
            LOG.error("No se pudo cargar el panel", e);
            lblBienvenida.setText("No se pudo cargar el resumen: " + e.getMessage());
        }
    }
}
