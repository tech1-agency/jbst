package jbst.foundation.domain.exceptions.random;

public class IllegalEnumException extends IllegalArgumentException {

    public IllegalEnumException(Class<?> enumClazz) {
        super("Please check enum: " + enumClazz);
    }
}
