package jbst.foundation.domain.properties;

import jbst.foundation.domain.properties.configs.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

@Slf4j
@ConfigurationProperties(
        prefix = "jbst",
        ignoreUnknownFields = false
)
@Data
public class JbstProperties implements PriorityOrdered {
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
    private MongodbSecurityJwtConfigs mongodbSecurityJwtConfigs;

    public static JbstProperties hardcoded() {
        var properties = new JbstProperties();
        properties.setMavenConfigs(MavenConfigs.hardcoded());
        properties.setServerConfigs(ServerConfigs.hardcoded());
        properties.setUtilitiesConfigs(UtilitiesConfigs.hardcoded());
        properties.setAsyncConfigs(AsyncConfigs.hardcoded());
        properties.setEventsConfigs(EventsConfigs.hardcoded());
        properties.setMvcConfigs(MvcConfigs.hardcoded());
        properties.setEmailConfigs(EmailConfigs.hardcoded());
        properties.setIncidentConfigs(IncidentConfigs.hardcoded());
        properties.setHardwareMonitoringConfigs(HardwareMonitoringConfigs.hardcoded());
        properties.setHardwareServerConfigs(HardwareServerConfigs.hardcoded());
        properties.setSecurityJwtConfigs(SecurityJwtConfigs.hardcoded());
        properties.setMongodbSecurityJwtConfigs(MongodbSecurityJwtConfigs.hardcoded());
        return properties;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    // ================================================================================================================
    // MERGED METHODS
    // ================================================================================================================
    public String getEmailConfirmationRedirectLink() {
        return "%s%s".formatted(
                this.serverConfigs.getWebclientURL(),
                this.securityJwtConfigs.getUsersTokensConfigs().getWebclientEmailConfirmationRedirectPath()
        );
    }

    public String getEmailConfirmationLink(@NotNull ServerProperties serverProperties, @NotNull String token) {
        return "%s%s/tokens/email/confirm?token=%s".formatted(
                this.serverConfigs.getServerURL() + serverProperties.getServlet().getContextPath(),
                this.mvcConfigs.getBasePathPrefix(),
                token
        );
    }

    public String getPasswordResetLink(@NotNull String token) {
        return "%s%s?token=%s".formatted(
                this.serverConfigs.getWebclientURL(),
                this.securityJwtConfigs.getUsersTokensConfigs().getWebclientPasswordResetPath(),
                token
        );
    }
}
