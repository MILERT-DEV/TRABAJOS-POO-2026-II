package pe.edu.upeu.systurismo.components;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Arma las columnas de un TableView a partir de un mapa "título -> propiedad".
 * Las propiedades anidadas se escriben con punto, por ejemplo "destino.nombre".
 * La columna cuyo título es "Estado" se dibuja como una etiqueta de color.
 */
public class TableViewHelper<T> {

    /** Columnas de datos + columna de acciones (editar / eliminar). */
    public void addColumnsInOrderWithSize(TableView<T> tableView, LinkedHashMap<String, ColumnInfo> columns,
                                          Consumer<T> updateAction, Consumer<T> deleteAction) {
        addColumns(tableView, columns);
        addActionColumn(tableView, updateAction, deleteAction);
    }

    /** Solo columnas de datos (para tablas con acciones propias). */
    public void addColumns(TableView<T> tableView, LinkedHashMap<String, ColumnInfo> columns) {
        for (Map.Entry<String, ColumnInfo> entry : columns.entrySet()) {
            TableColumn<T, Object> column = new TableColumn<>(entry.getKey());
            String field = entry.getValue().getField();

            if (field.contains(".")) {
                column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(valorAnidado(cellData.getValue(), field)));
            } else {
                column.setCellValueFactory(new PropertyValueFactory<>(field));
            }
            if ("Estado".equals(entry.getKey())) {
                column.setCellFactory(col -> new EstadoCell<>());
            }
            if (entry.getValue().getWidth() != null) {
                column.setPrefWidth(entry.getValue().getWidth());
            }
            tableView.getColumns().add(column);
        }
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    public void addActionColumn(TableView<T> tableView, Consumer<T> updateAction, Consumer<T> deleteAction) {
        TableColumn<T, Void> actionColumn = new TableColumn<>("Acciones");
        actionColumn.setSortable(false);
        actionColumn.setPrefWidth(110);
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button btnUpdate = botonIcono("/img/document-edit-icon.png", "Editar");
            private final Button btnDelete = botonIcono("/img/del-icon.png", "Eliminar");
            private final HBox cajaBotones = new HBox(8, btnUpdate, btnDelete);

            {
                cajaBotones.setAlignment(Pos.CENTER_LEFT);
                btnUpdate.setOnAction(event -> updateAction.accept(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(event -> deleteAction.accept(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : cajaBotones);
            }
        });
        tableView.getColumns().add(actionColumn);
    }

    private static Button botonIcono(String recurso, String ayuda) {
        ImageView icono = new ImageView(new Image(TableViewHelper.class.getResource(recurso).toExternalForm()));
        icono.setFitWidth(18);
        icono.setFitHeight(18);
        Button boton = new Button("", icono);
        boton.getStyleClass().add("btn-icono");
        boton.setTooltip(new Tooltip(ayuda));
        return boton;
    }

    private Object valorAnidado(T item, String field) {
        try {
            String[] ruta = field.split("\\.");
            Object valor = item.getClass().getMethod("get" + capitalize(ruta[0])).invoke(item);
            if (valor == null) {
                return "—";
            }
            Object anidado = valor.getClass().getMethod("get" + capitalize(ruta[1])).invoke(valor);
            return anidado != null ? anidado : "—";
        } catch (ReflectiveOperationException e) {
            return "—";
        }
    }

    private static String capitalize(String str) {
        return str == null || str.isEmpty() ? str : str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /** Celda que muestra el estado como una etiqueta de color (clases badge-activo, badge-pendiente...). */
    private static class EstadoCell<S> extends TableCell<S, Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            Label etiqueta = new Label(item.toString());
            etiqueta.getStyleClass().addAll("badge", "badge-" + item.toString().toLowerCase());
            setGraphic(etiqueta);
            setText(null);
        }
    }
}
