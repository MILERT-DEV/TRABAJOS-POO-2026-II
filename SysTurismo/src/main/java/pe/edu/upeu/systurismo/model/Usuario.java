package pe.edu.upeu.systurismo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.systurismo.enums.EstadoRegistro;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private Long idUsuario;
    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 4, max = 20, message = "El usuario debe tener entre 4 y 20 caracteres")
    private String usuario;
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombreCompleto;
    private String clave;
    @NotNull(message = "Seleccione un perfil")
    private Perfil perfil;
    @NotNull(message = "Seleccione el estado")
    private EstadoRegistro estado;
}
