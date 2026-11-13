package pe.edu.upeu.systurismo.service;

import pe.edu.upeu.systurismo.model.Paquete;

import java.util.List;

public interface IPaqueteService extends ICrudGenericoService<Paquete, Long> {
    /** Paquetes que se pueden reservar (estado activo). */
    List<Paquete> listarActivos();
}
