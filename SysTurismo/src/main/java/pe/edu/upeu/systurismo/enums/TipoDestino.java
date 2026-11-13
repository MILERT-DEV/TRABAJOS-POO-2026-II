package pe.edu.upeu.systurismo.enums;

public enum TipoDestino {
    PLAYA("Playa"),
    AVENTURA("Aventura"),
    CULTURAL("Cultural"),
    NATURALEZA("Naturaleza"),
    GASTRONOMIA("Gastronomía");

    private final String descripcion;

    TipoDestino(String descripcion) {
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
