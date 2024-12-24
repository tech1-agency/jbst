package jbst.foundation.domain.properties.configs.security.jwt;

import jbst.foundation.domain.properties.annotations.MandatoryProperty;
import jbst.foundation.domain.properties.annotations.MandatoryToggleProperty;
import jbst.foundation.domain.properties.configs.AbstractTogglePropertiesConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.CsrfConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.MessageBrokerRegistryConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.StompEndpointRegistryConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.WebsocketsFeaturesConfigs;
import jbst.foundation.utilities.random.RandomUtility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class WebsocketsConfigs extends AbstractTogglePropertiesConfigs {
    @MandatoryProperty
    private final boolean enabled;
    @MandatoryToggleProperty
    private final CsrfConfigs csrfConfigs;
    @MandatoryToggleProperty
    private final StompEndpointRegistryConfigs stompConfigs;
    @MandatoryToggleProperty
    private final MessageBrokerRegistryConfigs brokerConfigs;
    @MandatoryToggleProperty
    private WebsocketsFeaturesConfigs featuresConfigs;

    public static WebsocketsConfigs hardcoded() {
        return new WebsocketsConfigs(
                true,
                CsrfConfigs.hardcoded(),
                StompEndpointRegistryConfigs.hardcoded(),
                MessageBrokerRegistryConfigs.hardcoded(),
                WebsocketsFeaturesConfigs.hardcoded()
        );
    }

    public static WebsocketsConfigs random() {
        return new WebsocketsConfigs(
                RandomUtility.randomBoolean(),
                CsrfConfigs.random(),
                StompEndpointRegistryConfigs.random(),
                MessageBrokerRegistryConfigs.random(),
                WebsocketsFeaturesConfigs.random()
        );
    }

    @Override
    public boolean isParentPropertiesNode() {
        return false;
    }
}
