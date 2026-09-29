package pe.edu.upeu.mascotas.model;

public class Perro extends Mascota {

    public Perro(String nombre, String raza, String color, String sexo, int edad,
                 String propietario, String telefono, boolean vacunado) {
        super(nombre, raza, color, sexo, edad, propietario, telefono, vacunado);
    }

    @Override
    public String getEspecie() {
        return "Perro";
    }

    @Override
    public String hacerSonido() {
        return "Guau guau";
    }
}
