package pe.edu.upeu.systurismo.enums;

public enum EstadoRegistro {
    ACTIVO("Activo"),
    INACTIVO("Inactivo");

    private final String descripcion;

    EstadoRegistro(String descripcion) {
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
