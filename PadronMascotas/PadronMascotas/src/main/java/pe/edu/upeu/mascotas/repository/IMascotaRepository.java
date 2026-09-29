package pe.edu.upeu.mascotas.repository;

import pe.edu.upeu.mascotas.model.Mascota;

import java.util.List;

public interface IMascotaRepository {
    void guardar(Mascota mascota);
    void actualizar(Mascota mascota);
    void eliminar(int id);
    List<Mascota> listar();
}
