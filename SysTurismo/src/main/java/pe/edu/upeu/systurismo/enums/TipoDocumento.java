package pe.edu.upeu.systurismo.enums;

public enum TipoDocumento {
    DNI("DNI"),
    PASAPORTE("Pasaporte"),
    CARNET_EXTRANJERIA("Carné de extranjería");

    private final String descripcion;

    TipoDocumento(String descripcion) {
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
