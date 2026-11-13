package pe.edu.upeu.systurismo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos de una persona obtenidos al consultar un DNI. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDto {

    private String dni;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;

    /** Apellidos juntos: "Paterno Materno". */
    public String getApellidos() {
        String p = apellidoPaterno == null ? "" : apellidoPaterno;
        String m = apellidoMaterno == null ? "" : apellidoMaterno;
        return (p + " " + m).trim();
    }

    /** true si la consulta devolvió datos. */
    public boolean tieneDatos() {
        return nombre != null && !nombre.isBlank();
    }
}
