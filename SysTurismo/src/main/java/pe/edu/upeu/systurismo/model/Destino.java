package pe.edu.upeu.systurismo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;
import pe.edu.upeu.systurismo.enums.TipoDestino;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Destino {

    private Long idDestino;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombre;
    @NotBlank(message = "El país es obligatorio")
    @Size(max = 40, message = "Máximo 40 caracteres")
    private String pais;
    @NotBlank(message = "La ciudad o región es obligatoria")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String ciudad;
    @NotNull(message = "Seleccione el tipo de destino")
    private TipoDestino tipoDestino;
    @Size(max = 300, message = "Máximo 300 caracteres")
    private String descripcion;
    @NotNull(message = "Seleccione el estado")
    private EstadoRegistro estado;
}
