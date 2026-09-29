package pe.edu.upeu.mascotas.service;

import pe.edu.upeu.mascotas.model.Mascota;

import java.util.List;

public interface IMascotaService {
    void guardar(Mascota mascota);
    void actualizar(Mascota mascota);
    void eliminar(int id);
    List<Mascota> listar();
    List<Mascota> filtrar(String especie, String estadoVacunacion);
}
