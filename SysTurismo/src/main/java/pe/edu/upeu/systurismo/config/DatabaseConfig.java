package pe.edu.upeu.systurismo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Configuración de la base de datos H2 (embebida) con pool de conexiones HikariCP.
 *
 * <p>Lee {@code application.properties}:
 * <ul>
 *   <li>{@code db.url}, {@code db.username}, {@code db.password}</li>
 *   <li>{@code db.ddl.auto}: si es true, ejecuta los scripts al iniciar</li>
 *   <li>{@code db.ddl.script}: uno o varios scripts separados por coma</li>
 * </ul>
 */
public final class DatabaseConfig {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String PROPERTIES_FILE = "application.properties";
    private static final String DEFAULT_URL = "jdbc:h2:file:./data/systurismo";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_SCRIPT = "schema_turismo.sql";

    private static volatile HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    /** Inicializa el pool y ejecuta el DDL una sola vez. */
    public static synchronized void init() {
        if (dataSource != null) {
            return;
        }
        Properties props = loadProperties();
        String url = props.getProperty("db.url", DEFAULT_URL);

        HikariDataSource ds = createDataSource(url, props);
        try {
            if (Boolean.parseBoolean(props.getProperty("db.ddl.auto", "true"))) {
                for (String script : props.getProperty("db.ddl.script", DEFAULT_SCRIPT).split(",")) {
                    if (!script.isBlank()) {
                        runScript(ds, script.strip());
                    }
                }
            }
        } catch (RuntimeException e) {
            ds.close();
            throw e;
        }
        dataSource = ds;
        LOG.info("H2 listo: {}", url);
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            init();
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /** Cierra el pool al salir de la aplicación. */
    public static synchronized void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    private static HikariDataSource createDataSource(String url, Properties props) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("SysTurismoPool");
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(url);
        config.setUsername(props.getProperty("db.username", DEFAULT_USER));
        config.setPassword(props.getProperty("db.password", ""));
        // App de escritorio con un solo usuario: un pool pequeño es suficiente.
        config.setMaximumPoolSize(5);
        try {
            return new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw new IllegalStateException("No fue posible abrir la base de datos H2: " + url, e);
        }
    }

    private static Properties loadProperties() {
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/" + PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + PROPERTIES_FILE);
            }
            Properties props = new Properties();
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo " + PROPERTIES_FILE, e);
        }
    }

    /** Ejecuta el script DDL sentencia por sentencia (se separan por punto y coma). */
    private static void runScript(DataSource ds, String script) {
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/" + script)) {
            if (in == null) {
                LOG.warn("No se encontró el script DDL {}", script);
                return;
            }
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .filter(line -> !line.strip().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
                for (String statement : sql.split(";")) {
                    if (!statement.isBlank()) {
                        stmt.execute(statement);
                    }
                }
            }
            LOG.info("DDL ejecutado: {}", script);
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Error ejecutando el script " + script, e);
        }
    }
}
