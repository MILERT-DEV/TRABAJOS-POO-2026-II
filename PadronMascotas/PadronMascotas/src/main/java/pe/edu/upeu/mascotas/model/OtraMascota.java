package pe.edu.upeu.mascotas.model;

public class OtraMascota extends Mascota {

    public OtraMascota(String nombre, String raza, String color, String sexo, int edad,
                       String propietario, String telefono, boolean vacunado) {
        super(nombre, raza, color, sexo, edad, propietario, telefono, vacunado);
    }

    @Override
    public String getEspecie() {
        return "Otro";
    }

    @Override
    public String hacerSonido() {
        return "...";
    }
}
