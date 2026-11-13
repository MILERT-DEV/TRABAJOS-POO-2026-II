package pe.edu.upeu.systurismo.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.systurismo.enums.Dificultad;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paquete {

    private Long idPaquete;
    @NotBlank(message = "El nombre del paquete es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombre;
    @NotNull(message = "Seleccione un destino")
    private Destino destino;
    @NotNull(message = "Seleccione un guía")
    private Guia guia;
    @NotNull(message = "Ingrese la duración")
    @Min(value = 1, message = "La duración mínima es 1 día")
    @Max(value = 30, message = "La duración máxima es 30 días")
    private Integer duracionDias;
    @NotNull(message = "Ingrese el cupo máximo")
    @Min(value = 1, message = "El cupo mínimo es 1 persona")
    @Max(value = 60, message = "El cupo máximo es 60 personas")
    private Integer cupoMaximo;
    @NotNull(message = "Ingrese el precio")
    @DecimalMin(value = "1.00", message = "El precio debe ser mayor o igual a S/ 1.00")
    @Digits(integer = 7, fraction = 2, message = "Use hasta 2 decimales")
    private BigDecimal precio;
    @NotNull(message = "Seleccione la dificultad")
    private Dificultad dificultad;
    @Size(max = 300, message = "Máximo 300 caracteres")
    private String incluye;
    @NotNull(message = "Seleccione el estado")
    private EstadoRegistro estado;
}
