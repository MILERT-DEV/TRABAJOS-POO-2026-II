package pe.edu.upeu.mascotas.service;

import pe.edu.upeu.mascotas.model.Mascota;
import pe.edu.upeu.mascotas.repository.IMascotaRepository;

import java.util.ArrayList;
import java.util.List;

public class MascotaService implements IMascotaService {

    private final IMascotaRepository repositorio;

    public MascotaService(IMascotaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void guardar(Mascota mascota) {
        validar(mascota);
        repositorio.guardar(mascota);
    }

    @Override
    public void actualizar(Mascota mascota) {
        validar(mascota);
        repositorio.actualizar(mascota);
    }

    @Override
    public void eliminar(int id) {
        repositorio.eliminar(id);
    }

    @Override
    public List<Mascota> listar() {
        return repositorio.listar();
    }

    @Override
    public List<Mascota> filtrar(String especie, String estadoVacunacion) {
        List<Mascota> resultado = new ArrayList<>();
        for (Mascota m : repositorio.listar()) {
            boolean coincideEspecie = especie == null || especie.equals("Todas")
                    || m.getEspecie().equals(especie);
            boolean coincideEstado = estadoVacunacion == null || estadoVacunacion.equals("Todos")
                    || m.getEstadoVacunacion().equals(estadoVacunacion);
            if (coincideEspecie && coincideEstado) {
                resultado.add(m);
            }
        }
        return resultado;
    }

    private void validar(Mascota m) {
        if (m.getNombre() == null || m.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la mascota es obligatorio.");
        }
        if (m.getRaza() == null || m.getRaza().trim().isEmpty()) {
            throw new IllegalArgumentException("La raza es obligatoria.");
        }
        if (m.getColor() == null || m.getColor().trim().isEmpty()) {
            throw new IllegalArgumentException("El color es obligatorio.");
        }
        if (m.getSexo() == null || m.getSexo().trim().isEmpty()) {
            throw new IllegalArgumentException("Seleccione el sexo de la mascota.");
        }
        if (m.getEdad() < 0 || m.getEdad() > 40) {
            throw new IllegalArgumentException("La edad debe estar entre 0 y 40 años.");
        }
        if (m.getPropietario() == null || m.getPropietario().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del propietario es obligatorio.");
        }
        if (m.getTelefono() == null || !m.getTelefono().matches("\\d{9}")) {
            throw new IllegalArgumentException("El teléfono debe tener 9 dígitos.");
        }
    }
}
