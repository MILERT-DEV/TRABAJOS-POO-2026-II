package pe.edu.upeu.systurismo.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.upeu.systurismo.components.ColumnInfo;
import pe.edu.upeu.systurismo.components.PaginationBar;
import pe.edu.upeu.systurismo.components.TableViewHelper;
import pe.edu.upeu.systurismo.components.Toast;
import pe.edu.upeu.systurismo.components.ToltipCustom;
import pe.edu.upeu.systurismo.exception.ModelNotFoundException;
import pe.edu.upeu.systurismo.exception.ReglaNegocioException;
import pe.edu.upeu.systurismo.exception.ValidacionException;
import pe.edu.upeu.systurismo.repository.helper.PageResult;
import pe.edu.upeu.systurismo.service.ICrudGenericoService;
import pe.edu.upeu.systurismo.utils.FxUtil;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador base de las pantallas CRUD (patrón Template Method).
 *
 * <p>Aquí está todo lo que se repite: cargar la tabla con paginación y búsqueda, guardar o actualizar,
 * eliminar con confirmación, marcar los campos con error y mostrar el resultado de cada acción.
 * Cada pantalla concreta solo aporta lo propio: sus campos, sus columnas y cómo pasar de
 * formulario a entidad y de entidad a formulario.
 *
 * <p>El controlador no valida ni guarda por su cuenta: llama al servicio, que es quien aplica las reglas.
 */
public abstract class CrudBaseController<T, ID> {

    private static final Logger LOG = LoggerFactory.getLogger(CrudBaseController.class);

    @FXML protected TableView<T> tableView;
    @FXML protected TextField txtBuscar;
    @FXML protected Label lbnMsg;
    @FXML protected Label lblFormTitulo;
    @FXML protected HBox boxPaginacion;

    protected final ICrudGenericoService<T, ID> servicio;

    /** Errores detectados al leer el formulario (por ejemplo, un número mal escrito). */
    protected final Map<String, String> erroresLectura = new LinkedHashMap<>();

    private final ToltipCustom tooltips = new ToltipCustom();
    private final PaginationBar paginacion = new PaginationBar();
    private final PauseTransition esperaBusqueda = new PauseTransition(Duration.millis(300));
    private ID idEnEdicion;
    private int paginaActual = 0;

    protected CrudBaseController(ICrudGenericoService<T, ID> servicio) {
        this.servicio = servicio;
    }

    // ------------------------------------------------------ lo que define cada pantalla

    /** Nombre en minúsculas para los títulos: "destino", "guía"... */
    protected abstract String nombreEntidad();

    protected abstract LinkedHashMap<String, ColumnInfo> columnas();

    /** Propiedad de la entidad -> control del formulario. Sirve para marcar los errores. */
    protected abstract Map<String, Control> camposValidables();

    protected abstract T leerFormulario();

    protected abstract void cargarFormulario(T entidad);

    protected abstract void limpiarCampos();

    protected abstract ID idDe(T entidad);

    protected abstract void asignarId(T entidad, ID id);

    /** Se llama antes de armar la tabla: carga combos, restringe el teclado, etc. */
    protected void prepararFormulario() {
    }

    // ------------------------------------------------------------------ ciclo de vida

    @FXML
    public void initialize() {
        prepararFormulario();

        // Al escribir en un campo con error, el error desaparece.
        camposValidables().values().forEach(control -> {
            if (control instanceof TextInputControl texto) {
                texto.textProperty().addListener((obs, anterior, nuevo) -> tooltips.limpiarCampo(control));
            } else if (control instanceof ComboBoxBase<?> combo) {
                combo.valueProperty().addListener((obs, anterior, nuevo) -> tooltips.limpiarCampo(control));
            }
        });

        new TableViewHelper<T>().addColumnsInOrderWithSize(tableView, columnas(), this::editar, this::eliminar);
        tableView.setPlaceholder(new Label("No hay registros para mostrar"));

        boxPaginacion.getChildren().add(paginacion);
        HBox.setHgrow(paginacion, javafx.scene.layout.Priority.ALWAYS);
        paginacion.setOnCambio((pagina, tamano) -> listar(pagina));

        // Búsqueda: espera 300 ms después de la última tecla para no consultar en cada letra.
        esperaBusqueda.setOnFinished(e -> listar(0));
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> esperaBusqueda.playFromStart());

        nuevo();
        listar(0);
    }

    // ------------------------------------------------------------------------ lista

    protected void listar(int pagina) {
        try {
            PageResult<T> resultado = servicio.findPage(txtBuscar.getText(), pagina, paginacion.getTamano());
            if (resultado.items().isEmpty() && pagina > 0) {
                listar(pagina - 1);   // se borró el último registro de la página
                return;
            }
            tableView.getItems().setAll(resultado.items());
            paginacion.actualizar(resultado);
            paginaActual = resultado.page();
        } catch (RuntimeException e) {
            LOG.error("No se pudo cargar la lista de {}", nombreEntidad(), e);
            mostrarMensaje("No se pudo cargar la lista: " + e.getMessage(), true);
        }
    }

    // ------------------------------------------------------------------ acciones

    @FXML
    public void nuevo() {
        idEnEdicion = null;
        limpiarErrores();
        limpiarCampos();
        lblFormTitulo.setText("Nuevo " + nombreEntidad());
        lbnMsg.setText("");
    }

    @FXML
    public void cancelar() {
        nuevo();
    }

    protected void editar(T entidad) {
        limpiarErrores();
        idEnEdicion = idDe(entidad);
        cargarFormulario(entidad);
        lblFormTitulo.setText("Editar " + nombreEntidad());
        lbnMsg.setText("");
    }

    @FXML
    public void guardar() {
        erroresLectura.clear();
        T entidad = leerFormulario();
        Map<String, String> errores = new LinkedHashMap<>(erroresLectura);
        try {
            if (errores.isEmpty()) {
                boolean esNuevo = idEnEdicion == null;
                if (esNuevo) {
                    servicio.save(entidad);
                } else {
                    asignarId(entidad, idEnEdicion);
                    servicio.update(idEnEdicion, entidad);
                }
                Toast.exito(stage(), esNuevo ? "Se guardó correctamente" : "Se actualizó correctamente");
                nuevo();
                listar(esNuevo ? 0 : paginaActual);
                return;
            }
        } catch (ValidacionException e) {
            e.getErrores().forEach(errores::putIfAbsent);
        } catch (ReglaNegocioException | ModelNotFoundException e) {
            mostrarMensaje(e.getMessage(), true);
            Toast.error(stage(), e.getMessage());
            return;
        } catch (RuntimeException e) {
            LOG.error("Error al guardar {}", nombreEntidad(), e);
            mostrarMensaje("Ocurrió un error inesperado. Intente de nuevo.", true);
            Toast.error(stage(), "Ocurrió un error inesperado");
            return;
        }
        mostrarErrores(errores);
    }

    protected void eliminar(T entidad) {
        boolean acepto = FxUtil.confirmar(stage(), "Eliminar " + nombreEntidad(),
                "¿Desea eliminar este registro? Esta acción no se puede deshacer.");
        if (!acepto) {
            return;
        }
        try {
            ID id = idDe(entidad);
            servicio.delete(id);
            if (id.equals(idEnEdicion)) {
                nuevo();
            }
            Toast.exito(stage(), "Se eliminó correctamente");
            listar(paginaActual);
        } catch (ReglaNegocioException | ModelNotFoundException e) {
            Toast.error(stage(), e.getMessage());
            mostrarMensaje(e.getMessage(), true);
        } catch (RuntimeException e) {
            LOG.error("Error al eliminar {}", nombreEntidad(), e);
            Toast.error(stage(), "No se pudo eliminar el registro");
        }
    }

    // ----------------------------------------------------------- mensajes y errores

    protected void mostrarErrores(Map<String, String> errores) {
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

    protected void limpiarErrores() {
        camposValidables().values().forEach(tooltips::limpiarCampo);
    }

    protected void mostrarMensaje(String mensaje, boolean esError) {
        lbnMsg.setText(mensaje);
        lbnMsg.getStyleClass().removeAll("msg-error", "msg-ok");
        lbnMsg.getStyleClass().add(esError ? "msg-error" : "msg-ok");
    }

    protected Stage stage() {
        return (Stage) tableView.getScene().getWindow();
    }

    // -------------------------------------------- lectura de campos (para las subclases)

    protected String texto(TextInputControl campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    protected Integer entero(TextInputControl campo, String propiedad) {
        String valor = texto(campo);
        if (valor.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            erroresLectura.put(propiedad, "Ingrese un número entero válido");
            return null;
        }
    }

    protected BigDecimal decimal(TextInputControl campo, String propiedad) {
        String valor = texto(campo);
        if (valor.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException e) {
            erroresLectura.put(propiedad, "Ingrese un monto válido");
            return null;
        }
    }
}
