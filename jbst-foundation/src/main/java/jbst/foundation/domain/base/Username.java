package jbst.foundation.domain.base;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jbst.foundation.domain.constants.StringConstants;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static jbst.foundation.utilities.random.RandomUtility.randomString;
import static org.springframework.util.StringUtils.hasLength;

public record Username(@NotNull String value) {

    @JsonCreator
    public static Username of(String value) {
        return new Username(value);
    }

    public static Username cron() {
        return of("cron");
    }

    public static Username ops() {
        return of("ops");
    }

    public static Username hardcoded() {
        return of("jbs");
    }

    public static Username random() {
        return of(randomString());
    }

    public static Username unknown() {
        return of(StringConstants.UNKNOWN);
    }

    public static Username dash() {
        return of(StringConstants.DASH);
    }

    @SuppressWarnings("unused")
    public static Username hyphen() {
        return of(StringConstants.HYPHEN);
    }

    public static Username testsHardcoded() {
        return of("tech1");
    }

    public static List<String> asStrings(Set<Username> usernames) {
        return usernames.stream()
                .map(Username::value)
                .collect(Collectors.toList());
    }

    public static List<String> asStrings(List<Username> usernames) {
        return usernames.stream()
                .map(Username::value)
                .collect(Collectors.toList());
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

    @Target({
            ElementType.FIELD,
            ElementType.METHOD
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = ConstraintValidatorOnUsername.class)
    public @interface ValidUsername {
        String message() default "is invalid";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }

    public static class ConstraintValidatorOnUsername implements ConstraintValidator<ValidUsername, Username> {
        @Override
        public boolean isValid(Username username, ConstraintValidatorContext constraintValidatorContext) {
            return nonNull(username) && hasLength(username.value);
        }
    }
}
