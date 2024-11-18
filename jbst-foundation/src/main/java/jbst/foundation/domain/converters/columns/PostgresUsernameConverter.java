package jbst.foundation.domain.converters.columns;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jbst.foundation.domain.base.Username;

import static java.util.Objects.nonNull;

@Converter
public class PostgresUsernameConverter implements AttributeConverter<Username, String> {

    @Override
    public String convertToDatabaseColumn(Username username) {
        return nonNull(username) ? username.value() : null;
    }

    @Override
    public Username convertToEntityAttribute(String value) {
        return nonNull(value) ? Username.of(value) : null;
    }
}
