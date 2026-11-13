package pe.edu.upeu.systurismo.enums;

public enum MetodoPago {
    EFECTIVO("Efectivo"),
    TARJETA("Tarjeta"),
    TRANSFERENCIA("Transferencia"),
    YAPE_PLIN("Yape / Plin");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Los ComboBox y las tablas muestran este texto. */
    @Override
    public String toString() {
        return descripcion;
    }
}
