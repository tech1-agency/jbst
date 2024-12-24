package jbst.foundation.domain.properties.configs.security.jwt.websockets;

import jbst.foundation.domain.properties.annotations.NonMandatoryProperty;
import jbst.foundation.domain.properties.configs.AbstractPropertiesConfigs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class WebsocketsFeaturesConfigs extends AbstractPropertiesConfigs {
    @NonMandatoryProperty
    private WebsocketsFeatureConfigs hardwareConfigs;
    @NonMandatoryProperty
    private WebsocketsFeatureConfigs resetServerConfigs;

    public static WebsocketsFeaturesConfigs hardcoded() {
        return new WebsocketsFeaturesConfigs(
                WebsocketsFeatureConfigs.hardcoded(),
                WebsocketsFeatureConfigs.hardcoded()
        );
    }

    public static WebsocketsFeaturesConfigs random() {
        return new WebsocketsFeaturesConfigs(
                WebsocketsFeatureConfigs.random(),
                WebsocketsFeatureConfigs.random()
        );
    }

    @Override
    public boolean isParentPropertiesNode() {
        return false;
    }
}
