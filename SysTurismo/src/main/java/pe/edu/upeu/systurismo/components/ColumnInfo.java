package pe.edu.upeu.systurismo.components;

/** Describe una columna de la tabla: nombre de la propiedad del modelo y ancho preferido. */
public class ColumnInfo {
    private final String field;
    private final Double width;

    public ColumnInfo(String field, Double width) {
        this.field = field;
        this.width = width;
    }

    public String getField() {
        return field;
    }

    public Double getWidth() {
        return width;
    }
}
