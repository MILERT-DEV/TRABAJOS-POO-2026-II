package pe.edu.upeu.systurismo.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import pe.edu.upeu.systurismo.exception.ValidacionException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Valida una entidad con las anotaciones de Jakarta Validation (@NotBlank, @Size, @Email...).
 * Se usa desde la capa de servicio, así la regla vale sin importar desde dónde se llame.
 */
public final class ValidadorEntidades {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private ValidadorEntidades() {
    }

    public static <T> void validar(T entidad) {
        Set<ConstraintViolation<T>> violaciones = VALIDATOR.validate(entidad);
        if (violaciones.isEmpty()) {
            return;
        }
        Map<String, String> errores = new LinkedHashMap<>();
        violaciones.stream()
                .sorted(Comparator.comparing((ConstraintViolation<T> v) -> v.getPropertyPath().toString()))
                .forEach(v -> errores.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        throw new ValidacionException(errores);
    }
}
