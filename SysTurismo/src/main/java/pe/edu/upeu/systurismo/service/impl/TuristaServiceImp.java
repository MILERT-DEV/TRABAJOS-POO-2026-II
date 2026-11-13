package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.enums.TipoDocumento;
import pe.edu.upeu.systurismo.exception.ValidacionException;
import pe.edu.upeu.systurismo.model.Turista;
import pe.edu.upeu.systurismo.repository.ICrudGenericoRepository;
import pe.edu.upeu.systurismo.repository.TuristaRepository;
import pe.edu.upeu.systurismo.service.ITuristaService;

public class TuristaServiceImp extends CrudGenericoServiceImp<Turista, Long> implements ITuristaService {

    private final TuristaRepository repository;

    public TuristaServiceImp(TuristaRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ICrudGenericoRepository<Turista, Long> getRepo() {
        return repository;
    }

    /** Reglas que dependen de más de un campo o de otros registros. */
    @Override
    protected void reglasDeNegocio(Turista t) {
        String doc = t.getNroDocumento();
        if (t.getTipoDocumento() == TipoDocumento.DNI && !doc.matches("\\d{8}")) {
            throw new ValidacionException("nroDocumento", "El DNI debe tener exactamente 8 dígitos");
        }
        repository.buscarPorDocumento(doc).ifPresent(otro -> {
            if (!otro.getIdTurista().equals(t.getIdTurista())) {
                throw new ValidacionException("nroDocumento", "Ya existe un turista con ese documento");
            }
        });
    }
}
