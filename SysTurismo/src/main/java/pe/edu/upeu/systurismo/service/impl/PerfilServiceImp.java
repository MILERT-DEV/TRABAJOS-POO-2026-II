package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.model.Perfil;
import pe.edu.upeu.systurismo.repository.ICrudGenericoRepository;
import pe.edu.upeu.systurismo.repository.PerfilRepository;
import pe.edu.upeu.systurismo.service.IPerfilService;

public class PerfilServiceImp extends CrudGenericoServiceImp<Perfil, Long> implements IPerfilService {

    private final PerfilRepository repository;

    public PerfilServiceImp(PerfilRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ICrudGenericoRepository<Perfil, Long> getRepo() {
        return repository;
    }
}
