package jbst.foundation.configurations;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.annotation.PostConstruct;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.resources.actuator.BaseInfoResource;
import jbst.foundation.utilities.environment.EnvironmentUtility;
import jbst.foundation.utilities.environment.base.BaseEnvironmentUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

// Swagger
@OpenAPIDefinition(
        info = @Info(
                title = "${jbst.server-configs.name}",
                version = "${jbst.maven-configs.version}"
        )
)
// Spring
@Configuration
@EnableConfigurationProperties({
        JbstProperties.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationSpringBootServer {

    // Environment
    private final Environment environment;
    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getServerConfigs().assertProperties(new PropertyId("serverConfigs"));
        this.jbstProperties.getMavenConfigs().assertProperties(new PropertyId("mavenConfigs"));
    }

    @Bean
    public EnvironmentUtility environmentUtility() {
        return new BaseEnvironmentUtility(
                this.environment
        );
    }

    @Bean
    public BaseInfoResource baseInfoResource() {
        return new BaseInfoResource(
                this.environmentUtility(),
                this.jbstProperties
        );
    }
}
