package io.tech1.framework.foundation.domain.reflections;

import io.tech1.framework.foundation.domain.base.PropertyId;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.Arrays;

import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.uncapitalize;

// Lombok
@Data
public class ReflectionProperty {
    private static final String READABLE_PROPERTY = "%s: `%s`";

    private final Field field;
    private final String propertyName;
    private final PropertyId treePropertyId;
    private final Object propertyValue;
    private final String readableValue;

    public ReflectionProperty(@NotNull PropertyId propertyId, Field field, Object propertyValue) {
        this.field = field;
        this.propertyName = field.getName();
        this.treePropertyId = new PropertyId(uncapitalize(propertyId.value()) + "." + uncapitalize(this.propertyName));
        this.propertyValue = propertyValue;

        // supports only String[] and ZoneId (on 5+ cases refactoring or extraction required)
        var isArray = nonNull(this.propertyValue) && this.propertyValue.getClass().isArray();
        boolean isArrayOfStrings;
        if (isArray) {
            var array = (Object[]) this.propertyValue;
            isArrayOfStrings = array[0] instanceof String;
        } else {
            isArrayOfStrings = false;
        }
        var isZoneId = nonNull(this.propertyValue) && this.propertyValue instanceof ZoneId;

        if (isArrayOfStrings) {
            var castedPropertyValue = (String[]) this.propertyValue;
            this.readableValue = READABLE_PROPERTY.formatted(this.treePropertyId.value(), Arrays.toString(castedPropertyValue));
        } else if (isZoneId) {
            var castedPropertyValue = (ZoneId) this.propertyValue;
            this.readableValue = READABLE_PROPERTY.formatted(this.treePropertyId.value(), castedPropertyValue.getId());
        } else {
            this.readableValue = READABLE_PROPERTY.formatted(this.treePropertyId.value(), this.propertyValue);
        }
    }
}
