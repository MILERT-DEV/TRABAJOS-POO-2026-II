package pe.edu.upeu.systurismo.service;

import pe.edu.upeu.systurismo.dto.ModuloMenu;

import java.util.List;

public interface INavegacionService {
    /** Módulos del menú lateral que puede ver un perfil. */
    List<ModuloMenu> modulosPara(String perfil);
}
