package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.model.Paquete;
import pe.edu.upeu.systurismo.repository.ICrudGenericoRepository;
import pe.edu.upeu.systurismo.repository.PaqueteRepository;
import pe.edu.upeu.systurismo.service.IPaqueteService;

import java.util.List;

public class PaqueteServiceImp extends CrudGenericoServiceImp<Paquete, Long> implements IPaqueteService {

    private final PaqueteRepository repository;

    public PaqueteServiceImp(PaqueteRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ICrudGenericoRepository<Paquete, Long> getRepo() {
        return repository;
    }

    @Override
    public List<Paquete> listarActivos() {
        return repository.listarActivos();
    }
}
