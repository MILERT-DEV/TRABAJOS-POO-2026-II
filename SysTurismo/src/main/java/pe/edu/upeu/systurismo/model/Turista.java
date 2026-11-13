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
import pe.edu.upeu.systurismo.enums.TipoDocumento;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Turista {

    private Long idTurista;
    @NotNull(message = "Seleccione el tipo de documento")
    private TipoDocumento tipoDocumento;
    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 5, max = 15, message = "El documento debe tener entre 5 y 15 caracteres")
    private String nroDocumento;
    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String nombres;
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 60, message = "Máximo 60 caracteres")
    private String apellidos;
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 80, message = "Máximo 80 caracteres")
    private String email;
    @Pattern(regexp = "\\d{7,15}", message = "El teléfono debe tener entre 7 y 15 dígitos")
    private String telefono;
    @NotBlank(message = "La nacionalidad es obligatoria")
    @Size(max = 40, message = "Máximo 40 caracteres")
    private String nacionalidad;

    /** Nombre para mostrar en tablas y listas desplegables. */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
