package pe.edu.upeu.mascotas.repository;

import pe.edu.upeu.mascotas.model.Gato;
import pe.edu.upeu.mascotas.model.Mascota;
import pe.edu.upeu.mascotas.model.OtraMascota;
import pe.edu.upeu.mascotas.model.Perro;

import java.util.ArrayList;
import java.util.List;

public class MascotaRepository implements IMascotaRepository {

    private final List<Mascota> mascotas = new ArrayList<>();
    private int siguienteId = 1;

    public MascotaRepository() {

        guardar(new Perro("Firulais", "Mestizo", "Plomo", "Macho", 4, "Juan Pérez", "987654321", true));
        guardar(new OtraMascota("Conejo Blanco", "Enano", "Blanco", "Macho", 1, "Luis Mamani", "955444333", true));
    }

    @Override
    public void guardar(Mascota mascota) {
        mascota.setId(siguienteId);
        siguienteId++;
        mascotas.add(mascota);
    }

    @Override
    public void actualizar(Mascota mascota) {
        for (int i = 0; i < mascotas.size(); i++) {
            if (mascotas.get(i).getId() == mascota.getId()) {
                mascotas.set(i, mascota);
                return;
            }
        }
    }

    @Override
    public void eliminar(int id) {
        for (int i = 0; i < mascotas.size(); i++) {
            if (mascotas.get(i).getId() == id) {
                mascotas.remove(i);
                return;
            }
        }
    }

    @Override
    public List<Mascota> listar() {
        return new ArrayList<>(mascotas);
    }
}
