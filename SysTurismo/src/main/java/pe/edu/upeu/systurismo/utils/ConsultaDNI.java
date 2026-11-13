package pe.edu.upeu.systurismo.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pe.edu.upeu.systurismo.dto.PersonaDto;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consulta nombres y apellidos de una persona a partir de su DNI (web eldni.com).
 * Guarda en memoria los DNI ya encontrados para no repetir la consulta por internet.
 */
public class ConsultaDNI {

    private static final String URL = "https://eldni.com/pe/buscar-datos-por-dni";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/124.0 Safari/537.36";
    private static final int TIMEOUT_MS = 10_000;
    private static final Set<String> PARTICULAS = Set.of("de", "del", "la", "las", "los", "y", "van", "von");

    /** DNI ya encontrados durante esta sesión. */
    private static final Map<String, PersonaDto> CACHE = new ConcurrentHashMap<>();

    /**
     * Devuelve los datos del DNI. Si la persona no existe, el DTO vuelve vacío
     * (ver {@link PersonaDto#tieneDatos()}). Lanza IOException si no hay conexión.
     * Los nombres se devuelven en formato "Juan Carlos", no en mayúsculas.
     */
    public PersonaDto consultarDNI(String dni) throws IOException {
        PersonaDto enCache = CACHE.get(dni);
        if (enCache != null) {
            return enCache;
        }

        // 1) GET para obtener el _token y la sesión
        Connection.Response getResponse = Jsoup.connect(URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .method(Connection.Method.GET)
                .execute();
        String token = getResponse.parse().select("input[name=_token]").attr("value");

        // 2) POST con el DNI y el token
        Connection.Response postResponse = Jsoup.connect(URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .cookies(getResponse.cookies())
                .data("_token", token)
                .data("dni", dni)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();

        // 3) Leer la primera fila de la tabla de resultados
        PersonaDto persona = new PersonaDto();
        Document resultDoc = Jsoup.parse(postResponse.body());
        Element fila = resultDoc.select("table tbody tr").first();
        if (fila != null) {
            Elements celdas = fila.select("td");
            if (celdas.size() >= 4) {
                persona.setDni(celdas.get(0).text());
                persona.setNombre(aTitulo(celdas.get(1).text()));
                persona.setApellidoPaterno(aTitulo(celdas.get(2).text()));
                persona.setApellidoMaterno(aTitulo(celdas.get(3).text()));
            }
        }
        if (persona.tieneDatos()) {
            CACHE.put(dni, persona);
        }
        return persona;
    }

    /** "JUAN CARLOS DE LA CRUZ" -> "Juan Carlos de la Cruz". */
    static String aTitulo(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] palabras = texto.trim().toLowerCase().split("\\s+");
        for (int i = 0; i < palabras.length; i++) {
            String p = palabras[i];
            if (i > 0) {
                sb.append(' ');
            }
            if (i > 0 && PARTICULAS.contains(p)) {
                sb.append(p);
            } else {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
            }
        }
        return sb.toString();
    }
}
