package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.dto.ModuloMenu;
import pe.edu.upeu.systurismo.service.INavegacionService;

import java.util.List;
import java.util.Set;

public class NavegacionServiceImp implements INavegacionService {

    private static final ModuloMenu INICIO = new ModuloMenu("inicio", "Inicio", "⌂", "/view/dashboard.fxml");
    private static final ModuloMenu RESERVAS = new ModuloMenu("reservas", "Reservas", "✈", "/view/main_reserva.fxml");
    private static final ModuloMenu TURISTAS = new ModuloMenu("turistas", "Turistas", "☺", "/view/main_turista.fxml");
    private static final ModuloMenu PAQUETES = new ModuloMenu("paquetes", "Paquetes", "▣", "/view/main_paquete.fxml");
    private static final ModuloMenu DESTINOS = new ModuloMenu("destinos", "Destinos", "⚑", "/view/main_destino.fxml");
    private static final ModuloMenu GUIAS = new ModuloMenu("guias", "Guías", "☰", "/view/main_guia.fxml");
    private static final ModuloMenu USUARIOS = new ModuloMenu("usuarios", "Usuarios", "⚙", "/view/main_usuario.fxml");

    private static final Set<String> PERFILES_ADMIN = Set.of("Root", "Administrador");

    @Override
    public List<ModuloMenu> modulosPara(String perfil) {
        if (PERFILES_ADMIN.contains(perfil)) {
            return List.of(INICIO, RESERVAS, TURISTAS, PAQUETES, DESTINOS, GUIAS, USUARIOS);
        }
        // Agente: atiende clientes y reservas, no administra catálogos ni usuarios.
        return List.of(INICIO, RESERVAS, TURISTAS);
    }
}
