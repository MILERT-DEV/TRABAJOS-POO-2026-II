package pe.edu.upeu.systurismo.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.systurismo.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    private Long idPago;
    private Long idReserva;
    @NotNull(message = "Ingrese el monto")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;
    @NotNull(message = "Seleccione el método de pago")
    private MetodoPago metodo;
    private LocalDate fechaPago;
}
