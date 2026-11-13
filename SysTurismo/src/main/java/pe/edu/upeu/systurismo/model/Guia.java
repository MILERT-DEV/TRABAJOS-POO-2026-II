package pe.edu.upeu.systurismo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class Guia {

    private Long idGuia;
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String nombres;
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;
    @Pattern(regexp = "9\\d{8}", message = "El celular debe tener 9 dígitos y empezar con 9")
    private String telefono;
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String email;
    @NotBlank(message = "Indique al menos un idioma")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String idiomas;
    @NotNull(message = "Seleccione el estado")
    private EstadoRegistro estado;
}
