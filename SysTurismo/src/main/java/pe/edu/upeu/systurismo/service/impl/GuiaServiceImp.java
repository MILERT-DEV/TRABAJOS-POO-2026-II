package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.exception.ValidacionException;
import pe.edu.upeu.systurismo.model.Guia;
import pe.edu.upeu.systurismo.repository.GuiaRepository;
import pe.edu.upeu.systurismo.repository.ICrudGenericoRepository;
import pe.edu.upeu.systurismo.service.IGuiaService;

public class GuiaServiceImp extends CrudGenericoServiceImp<Guia, Long> implements IGuiaService {

    private final GuiaRepository repository;

    public GuiaServiceImp(GuiaRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ICrudGenericoRepository<Guia, Long> getRepo() {
        return repository;
    }

    @Override
    protected void reglasDeNegocio(Guia g) {
        repository.buscarPorDni(g.getDni()).ifPresent(otro -> {
            if (!otro.getIdGuia().equals(g.getIdGuia())) {
                throw new ValidacionException("dni", "Ya existe un guía con ese DNI");
            }
        });
    }
}
