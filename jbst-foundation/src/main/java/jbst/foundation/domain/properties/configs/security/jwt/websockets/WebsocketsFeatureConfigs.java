package jbst.foundation.domain.properties.configs.security.jwt.websockets;

import jbst.foundation.domain.properties.annotations.MandatoryProperty;
import jbst.foundation.domain.properties.annotations.MandatoryToggleProperty;
import jbst.foundation.domain.properties.base.AbstractTogglePropertyConfigs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import static jbst.foundation.utilities.random.RandomUtility.randomBoolean;
import static jbst.foundation.utilities.random.RandomUtility.randomString;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class WebsocketsFeatureConfigs extends AbstractTogglePropertyConfigs {
    @MandatoryProperty
    private final boolean enabled;
    @MandatoryToggleProperty
    private String userDestination;

    public static WebsocketsFeatureConfigs hardcoded() {
        return new WebsocketsFeatureConfigs(true, "/accounts");
    }

    public static WebsocketsFeatureConfigs random() {
        return new WebsocketsFeatureConfigs(randomBoolean(), randomString());
    }

    public static WebsocketsFeatureConfigs enabled() {
        return hardcoded();
    }

    public static WebsocketsFeatureConfigs disabled() {
        return new WebsocketsFeatureConfigs(false, null);
    }
}
