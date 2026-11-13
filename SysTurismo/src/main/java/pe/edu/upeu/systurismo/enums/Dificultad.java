package pe.edu.upeu.systurismo.enums;

public enum Dificultad {
    FACIL("Fácil"),
    MODERADA("Moderada"),
    EXIGENTE("Exigente");

    private final String descripcion;

    Dificultad(String descripcion) {
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
