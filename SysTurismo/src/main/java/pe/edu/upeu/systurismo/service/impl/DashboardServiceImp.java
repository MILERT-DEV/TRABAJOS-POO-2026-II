package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.dto.ResumenDashboard;
import pe.edu.upeu.systurismo.repository.DashboardRepository;
import pe.edu.upeu.systurismo.service.IDashboardService;

public class DashboardServiceImp implements IDashboardService {

    private final DashboardRepository repository;

    public DashboardServiceImp(DashboardRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResumenDashboard resumen() {
        return repository.obtenerResumen();
    }
}
