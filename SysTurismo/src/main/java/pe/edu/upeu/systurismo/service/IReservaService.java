package pe.edu.upeu.systurismo.service;

import pe.edu.upeu.systurismo.enums.MetodoPago;
import pe.edu.upeu.systurismo.model.Paquete;
import pe.edu.upeu.systurismo.model.Reserva;
import pe.edu.upeu.systurismo.repository.helper.PageResult;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Las reservas no usan el CRUD genérico: no se editan ni se borran, se cancelan.
 * Por eso tienen su propia interfaz con solo lo que la pantalla necesita.
 */
public interface IReservaService {

    PageResult<Reserva> findPage(String filtro, int page, int size);

    /** Registra la reserva y el adelanto (puede ser cero) en una transacción. */
    Reserva reservar(Reserva reserva, BigDecimal adelanto, MetodoPago metodo);

    Reserva registrarPago(Long idReserva, BigDecimal monto, MetodoPago metodo);

    Reserva cancelar(Long idReserva);

    int cuposDisponibles(Paquete paquete, LocalDate fechaViaje);

    BigDecimal calcularTotal(Paquete paquete, int personas);

    /** Adelanto mínimo para que la reserva quede confirmada. */
    BigDecimal adelantoMinimo(BigDecimal total);
}
