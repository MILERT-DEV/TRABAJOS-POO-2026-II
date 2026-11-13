package pe.edu.upeu.systurismo.components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import pe.edu.upeu.systurismo.repository.helper.PageResult;

import java.util.function.BiConsumer;

/**
 * Barra de paginación reutilizable: "Mostrando 1–10 de 42", selector de tamaño y botones anterior/siguiente.
 * No consulta datos: avisa con {@link #setOnCambio} cuando el usuario pide otra página o cambia el tamaño,
 * y la pantalla vuelve a consultar y llama a {@link #actualizar}.
 */
public class PaginationBar extends HBox {

    private final Label lblInfo = new Label();
    private final Label lblPagina = new Label();
    private final Button btnAnterior = new Button("‹");
    private final Button btnSiguiente = new Button("›");
    private final ComboBox<Integer> cbxTamano = new ComboBox<>();

    private int pagina = 0;
    private int totalPaginas = 1;
    private boolean actualizando = false;
    private BiConsumer<Integer, Integer> onCambio = (p, t) -> { };

    public PaginationBar() {
        getStyleClass().add("paginacion");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);

        lblInfo.getStyleClass().add("paginacion-info");
        lblPagina.getStyleClass().add("paginacion-info");
        btnAnterior.getStyleClass().add("btn-pagina");
        btnSiguiente.getStyleClass().add("btn-pagina");

        cbxTamano.getItems().addAll(5, 10, 20, 50);
        cbxTamano.setValue(10);
        Label lblPorPagina = new Label("por página");
        lblPorPagina.getStyleClass().add("paginacion-info");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        getChildren().addAll(lblInfo, espacio, cbxTamano, lblPorPagina, btnAnterior, lblPagina, btnSiguiente);

        btnAnterior.setOnAction(e -> {
            if (pagina > 0) {
                onCambio.accept(pagina - 1, getTamano());
            }
        });
        btnSiguiente.setOnAction(e -> {
            if (pagina < totalPaginas - 1) {
                onCambio.accept(pagina + 1, getTamano());
            }
        });
        cbxTamano.valueProperty().addListener((obs, anterior, nuevo) -> {
            if (!actualizando) {
                onCambio.accept(0, getTamano());
            }
        });
    }

    public void setOnCambio(BiConsumer<Integer, Integer> onCambio) {
        this.onCambio = onCambio;
    }

    public int getTamano() {
        Integer valor = cbxTamano.getValue();
        return valor == null ? 10 : valor;
    }

    public void actualizar(PageResult<?> resultado) {
        actualizando = true;
        pagina = resultado.page();
        totalPaginas = resultado.totalPages();
        long desde = resultado.total() == 0 ? 0 : (long) resultado.page() * resultado.size() + 1;
        long hasta = Math.min(resultado.total(), (long) (resultado.page() + 1) * resultado.size());
        lblInfo.setText("Mostrando " + desde + "–" + hasta + " de " + resultado.total());
        lblPagina.setText("Página " + (pagina + 1) + " de " + totalPaginas);
        btnAnterior.setDisable(pagina <= 0);
        btnSiguiente.setDisable(pagina >= totalPaginas - 1);
        actualizando = false;
    }
}
