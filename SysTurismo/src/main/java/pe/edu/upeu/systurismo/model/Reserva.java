package pe.edu.upeu.systurismo.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.systurismo.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {
    private Long idReserva;
    @NotNull(message = "Seleccione un turista")
    private Turista turista;
    @NotNull(message = "Seleccione un paquete")
    private Paquete paquete;
    private Usuario usuario;
    private LocalDate fechaReserva;
    @NotNull(message = "Seleccione la fecha de viaje")
    private LocalDate fechaViaje;
    @NotNull(message = "Ingrese la cantidad de personas")
    @Min(value = 1, message = "Debe viajar al menos 1 persona")
    @Max(value = 60, message = "Máximo 60 personas por reserva")
    private Integer numPersonas;
    private BigDecimal precioTotal;
    private EstadoReserva estado;
    /** Suma de los pagos registrados (se calcula en la consulta). */
    private BigDecimal pagado;

    public BigDecimal getSaldo() {
        BigDecimal total = precioTotal == null ? BigDecimal.ZERO : precioTotal;
        BigDecimal abonado = pagado == null ? BigDecimal.ZERO : pagado;
        return total.subtract(abonado);
    }
}
