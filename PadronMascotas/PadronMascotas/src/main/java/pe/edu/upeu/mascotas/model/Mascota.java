package pe.edu.upeu.mascotas.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Mascota {

    private int id;
    private String nombre;
    private String raza;
    private String color;
    private String sexo;
    private int edad;
    private String propietario;
    private String telefono;
    private boolean vacunado;

    public Mascota(String nombre, String raza, String color, String sexo, int edad,
                   String propietario, String telefono, boolean vacunado) {
        this.nombre = nombre;
        this.raza = raza;
        this.color = color;
        this.sexo = sexo;
        this.edad = edad;
        this.propietario = propietario;
        this.telefono = telefono;
        this.vacunado = vacunado;
    }

    public abstract String getEspecie();

    public abstract String hacerSonido();

    public String getEstadoVacunacion() {
        if (vacunado) {
            return "Al día";
        }
        return "Pendiente";
    }
}
