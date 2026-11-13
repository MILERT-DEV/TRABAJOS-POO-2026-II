package pe.edu.upeu.systurismo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Datos que muestra el panel principal. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDashboard {

    private long totalTuristas;
    private long paquetesActivos;
    private long reservasDelMes;
    private BigDecimal ingresosCobrados = BigDecimal.ZERO;
    /** Estado -> cantidad de reservas. */
    private Map<String, Long> reservasPorEstado = new LinkedHashMap<>();
    /** Paquete -> monto cobrado (los 5 que más ingresan). */
    private Map<String, BigDecimal> ingresosPorPaquete = new LinkedHashMap<>();
    private List<Salida> proximasSalidas = List.of();

    public record Salida(LocalDate fecha, String paquete, long personas) {
    }
}
