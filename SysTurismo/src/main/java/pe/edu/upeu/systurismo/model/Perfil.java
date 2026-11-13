package pe.edu.upeu.systurismo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {

    private Long idPerfil;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 20, message = "Máximo 20 caracteres")
    private String nombre;
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10, message = "Máximo 10 caracteres")
    private String codigo;
}
