package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.model.Destino;
import pe.edu.upeu.systurismo.repository.ICrudGenericoRepository;
import pe.edu.upeu.systurismo.repository.DestinoRepository;
import pe.edu.upeu.systurismo.service.IDestinoService;

public class DestinoServiceImp extends CrudGenericoServiceImp<Destino, Long> implements IDestinoService {

    private final DestinoRepository repository;

    public DestinoServiceImp(DestinoRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ICrudGenericoRepository<Destino, Long> getRepo() {
        return repository;
    }
}
