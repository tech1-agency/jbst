package jbst.foundation.domain.properties;

import jbst.foundation.domain.properties.configs.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

@Slf4j
@ConfigurationProperties(
        prefix = "tech1",
        ignoreUnknownFields = false
)
@Data
public class ApplicationFrameworkProperties implements PriorityOrdered {
    private ServerConfigs serverConfigs;
    private MavenConfigs mavenConfigs;
    private UtilitiesConfigs utilitiesConfigs;
    private AsyncConfigs asyncConfigs;
    private EventsConfigs eventsConfigs;
    private MvcConfigs mvcConfigs;
    private EmailConfigs emailConfigs;
    private IncidentConfigs incidentConfigs;
    private HardwareMonitoringConfigs hardwareMonitoringConfigs;
    private HardwareServerConfigs hardwareServerConfigs;
    private SecurityJwtConfigs securityJwtConfigs;
    private SecurityJwtWebsocketsConfigs securityJwtWebsocketsConfigs;
    private MongodbSecurityJwtConfigs mongodbSecurityJwtConfigs;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
