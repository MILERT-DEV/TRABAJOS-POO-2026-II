package pe.edu.upeu.systurismo.service.impl;

import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.enums.EstadoReserva;
import pe.edu.upeu.systurismo.enums.MetodoPago;
import pe.edu.upeu.systurismo.exception.ModelNotFoundException;
import pe.edu.upeu.systurismo.exception.ReglaNegocioException;
import pe.edu.upeu.systurismo.exception.ValidacionException;
import pe.edu.upeu.systurismo.model.Pago;
import pe.edu.upeu.systurismo.model.Paquete;
import pe.edu.upeu.systurismo.model.Reserva;
import pe.edu.upeu.systurismo.repository.ReservaRepository;
import pe.edu.upeu.systurismo.repository.helper.PageResult;
import pe.edu.upeu.systurismo.service.IReservaService;
import pe.edu.upeu.systurismo.utils.ValidadorEntidades;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class ReservaServiceImp implements IReservaService {

    /** La reserva se confirma cuando se paga al menos este porcentaje del total. */
    private static final BigDecimal PORCENTAJE_CONFIRMACION = new BigDecimal("0.30");

    private final ReservaRepository repository;

    public ReservaServiceImp(ReservaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Reserva> findPage(String filtro, int page, int size) {
        return repository.findPage(filtro, page, size);
    }

    @Override
    public Reserva reservar(Reserva reserva, BigDecimal adelanto, MetodoPago metodo) {
        ValidadorEntidades.validar(reserva);

        Paquete paquete = reserva.getPaquete();
        if (paquete.getEstado() != null && paquete.getEstado() != EstadoRegistro.ACTIVO) {
            throw new ValidacionException("paquete", "Ese paquete no está disponible");
        }
        if (!reserva.getFechaViaje().isAfter(LocalDate.now())) {
            throw new ValidacionException("fechaViaje", "La fecha de viaje debe ser posterior a hoy");
        }

        // El total siempre se calcula aquí, nunca se confía en el valor que mande la pantalla.
        BigDecimal total = calcularTotal(paquete, reserva.getNumPersonas());
        BigDecimal abono = adelanto == null ? BigDecimal.ZERO : adelanto;
        if (abono.signum() < 0) {
            throw new ValidacionException("adelanto", "El adelanto no puede ser negativo");
        }
        if (abono.compareTo(total) > 0) {
            throw new ValidacionException("adelanto", "El adelanto no puede superar el total (" + total + ")");
        }
        if (abono.signum() > 0 && metodo == null) {
            throw new ValidacionException("metodoPago", "Seleccione el método de pago");
        }

        reserva.setFechaReserva(LocalDate.now());
        reserva.setPrecioTotal(total);
        reserva.setEstado(abono.compareTo(adelantoMinimo(total)) >= 0 ? EstadoReserva.CONFIRMADA : EstadoReserva.PENDIENTE);

        Pago pago = abono.signum() > 0
                ? Pago.builder().monto(abono).metodo(metodo).fechaPago(LocalDate.now()).build()
                : null;
        return repository.registrarConPago(reserva, pago);
    }

    @Override
    public Reserva registrarPago(Long idReserva, BigDecimal monto, MetodoPago metodo) {
        Reserva reserva = buscar(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReglaNegocioException("La reserva está cancelada y no admite pagos");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new ReglaNegocioException("Ingrese un monto mayor a cero");
        }
        if (metodo == null) {
            throw new ReglaNegocioException("Seleccione el método de pago");
        }
        if (monto.compareTo(reserva.getSaldo()) > 0) {
            throw new ReglaNegocioException("El monto supera el saldo pendiente (" + reserva.getSaldo() + ")");
        }
        BigDecimal pagadoNuevo = reserva.getPagado().add(monto);
        EstadoReserva nuevoEstado = pagadoNuevo.compareTo(adelantoMinimo(reserva.getPrecioTotal())) >= 0
                ? EstadoReserva.CONFIRMADA : reserva.getEstado();

        Pago pago = Pago.builder().idReserva(idReserva).monto(monto).metodo(metodo).fechaPago(LocalDate.now()).build();
        repository.registrarPago(pago, nuevoEstado);
        return buscar(idReserva);
    }

    @Override
    public Reserva cancelar(Long idReserva) {
        Reserva reserva = buscar(idReserva);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReglaNegocioException("La reserva ya estaba cancelada");
        }
        repository.cambiarEstado(idReserva, EstadoReserva.CANCELADA);
        return buscar(idReserva);
    }

    @Override
    public int cuposDisponibles(Paquete paquete, LocalDate fechaViaje) {
        int ocupados = repository.personasReservadas(paquete.getIdPaquete(), fechaViaje);
        return Math.max(paquete.getCupoMaximo() - ocupados, 0);
    }

    @Override
    public BigDecimal calcularTotal(Paquete paquete, int personas) {
        return paquete.getPrecio().multiply(BigDecimal.valueOf(personas)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal adelantoMinimo(BigDecimal total) {
        return total.multiply(PORCENTAJE_CONFIRMACION).setScale(2, RoundingMode.HALF_UP);
    }

    private Reserva buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ModelNotFoundException("La reserva ya no existe (id " + id + ")"));
    }
}
