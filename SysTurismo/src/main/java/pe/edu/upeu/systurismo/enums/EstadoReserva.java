package pe.edu.upeu.systurismo.enums;

public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoReserva(String descripcion) {
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
