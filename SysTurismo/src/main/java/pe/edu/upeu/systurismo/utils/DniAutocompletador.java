package pe.edu.upeu.systurismo.utils;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import pe.edu.upeu.systurismo.dto.PersonaDto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Autocompleta un formulario al escribir un DNI de 8 dígitos.
 * Se usa igual en Turistas y Guías: cada pantalla solo dice qué hacer con los datos encontrados.
 *
 * <p>La consulta corre en un hilo aparte (la ventana no se congela). Si falla, se puede
 * reintentar presionando Enter en el campo del DNI.
 */
public class DniAutocompletador {

    /** Un solo hilo de fondo para todas las consultas; no impide cerrar la aplicación. */
    private static final ExecutorService HILO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "consulta-dni");
        t.setDaemon(true);
        return t;
    });

    private final TextField campoDni;
    private final BooleanSupplier habilitado;
    private final Consumer<PersonaDto> alEncontrar;
    private final BiConsumer<String, Boolean> alMensaje;

    private boolean pausado = false;
    private String dniEnConsulta;

    /**
     * @param campoDni    campo donde se escribe el DNI
     * @param habilitado  indica si en este momento corresponde consultar (p. ej. tipo de documento = DNI)
     * @param alEncontrar qué hacer con los datos de la persona encontrada
     * @param alMensaje   muestra un mensaje al usuario: (texto, esError)
     */
    public DniAutocompletador(TextField campoDni, BooleanSupplier habilitado,
                              Consumer<PersonaDto> alEncontrar, BiConsumer<String, Boolean> alMensaje) {
        this.campoDni = campoDni;
        this.habilitado = habilitado;
        this.alEncontrar = alEncontrar;
        this.alMensaje = alMensaje;

        campoDni.textProperty().addListener((obs, ant, nuevo) -> buscar());
        campoDni.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                dniEnConsulta = null; // permite repetir la misma consulta
                buscar();
            }
        });
    }

    /** Vuelve a evaluar si hay que consultar (por ejemplo, al cambiar el tipo de documento). */
    public void buscar() {
        if (pausado || !habilitado.getAsBoolean()) {
            return;
        }
        String dni = campoDni.getText() == null ? "" : campoDni.getText().trim();
        if (!dni.matches("\\d{8}")) {
            dniEnConsulta = null; // al borrar y volver a escribir el mismo DNI, se consulta de nuevo
            return;
        }
        if (dni.equals(dniEnConsulta)) {
            return;
        }
        dniEnConsulta = dni;
        alMensaje.accept("Buscando DNI...", false);

        HILO.execute(() -> {
            PersonaDto persona = null;
            boolean fallo = false;
            try {
                persona = new ConsultaDNI().consultarDNI(dni);
            } catch (Exception ex) {
                fallo = true;
            }
            final PersonaDto resultado = persona;
            final boolean huboError = fallo;
            Platform.runLater(() -> {
                // Si el usuario cambió el DNI mientras se consultaba, se ignora esta respuesta.
                if (!dni.equals(campoDni.getText())) {
                    return;
                }
                if (huboError) {
                    dniEnConsulta = null; // permite reintentar con Enter
                    alMensaje.accept("No se pudo consultar el DNI (¿sin internet?). Presione Enter para reintentar o escriba los datos.", true);
                } else if (resultado == null || !resultado.tieneDatos()) {
                    alMensaje.accept("DNI no encontrado. Ingrese los datos manualmente.", true);
                } else {
                    alEncontrar.accept(resultado);
                    alMensaje.accept("Datos completados desde el DNI.", false);
                }
            });
        });
    }

    /** Ejecuta una acción (como cargar un registro para editar) sin disparar consultas. */
    public void sinConsultar(Runnable accion) {
        pausado = true;
        try {
            accion.run();
        } finally {
            pausado = false;
            dniEnConsulta = null;
        }
    }
}
