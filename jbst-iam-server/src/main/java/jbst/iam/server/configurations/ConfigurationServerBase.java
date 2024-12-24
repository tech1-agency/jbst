package jbst.iam.server.configurations;

import jakarta.annotation.PostConstruct;
import jbst.foundation.domain.base.PropertyId;
import jbst.iam.server.base.properties.ServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        // -------------------------------------------------------------------------------------------------------------
        "jbst.iam.server.base"
        // -------------------------------------------------------------------------------------------------------------
})
@EnableConfigurationProperties({
        ServerProperties.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationServerBase {

    // Properties
    private final ServerProperties serverProperties;

    @PostConstruct
    public void init() {
        this.serverProperties.getServerConfigs().assertProperties(new PropertyId("serverConfigs"));
    }
}
