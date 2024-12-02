package jbst.foundation.domain.properties.configs.security.jwt;

import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.annotations.MandatoryMapProperty;
import jbst.foundation.domain.properties.annotations.MandatoryProperty;
import jbst.foundation.domain.properties.base.AbstractPropertyConfigs;
import jbst.foundation.domain.properties.base.SecurityJwtIncidentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.EnumMap;
import java.util.Map;

import static java.lang.Boolean.TRUE;
import static jbst.foundation.domain.properties.base.SecurityJwtIncidentType.*;
import static jbst.foundation.utilities.random.RandomUtility.getEnumMapMappedRandomBoolean;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class IncidentsConfigs extends AbstractPropertyConfigs {
    @MandatoryProperty
    @MandatoryMapProperty(propertyName = "typesConfigs", keySetClass = SecurityJwtIncidentType.class)
    private final Map<SecurityJwtIncidentType, Boolean> typesConfigs;

    public static IncidentsConfigs hardcoded() {
        return new IncidentsConfigs(
                new EnumMap<>(
                        Map.ofEntries(
                                Map.entry(AUTHENTICATION_LOGIN, true),
                                Map.entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD, false),
                                Map.entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD, true),
                                Map.entry(AUTHENTICATION_LOGOUT, false),
                                Map.entry(AUTHENTICATION_LOGOUT_MIN, false),
                                Map.entry(SESSION_REFRESHED, true),
                                Map.entry(SESSION_EXPIRED, false),
                                Map.entry(REGISTER0, true),
                                Map.entry(REGISTER0_FAILURE, true),
                                Map.entry(REGISTER1, true),
                                Map.entry(REGISTER1_FAILURE, true)
                        )
                )
        );
    }

    public static IncidentsConfigs random() {
        return new IncidentsConfigs(getEnumMapMappedRandomBoolean(SecurityJwtIncidentType.values()));
    }

    @Override
    public void assertProperties(PropertyId propertyId) {
        super.assertProperties(propertyId);

        var loginFailureUsernamePassword = this.typesConfigs.get(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD);
        var loginFailureUsernameMaskedPassword = this.typesConfigs.get(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD);

        if (TRUE.equals(loginFailureUsernamePassword) && TRUE.equals(loginFailureUsernameMaskedPassword)) {
            throw new IllegalArgumentException("Please configure login failure incident feature. Only one feature type could be enabled");
        }
    }

    public boolean isEnabled(SecurityJwtIncidentType type) {
        return TRUE.equals(this.typesConfigs.get(type));
    }
}
