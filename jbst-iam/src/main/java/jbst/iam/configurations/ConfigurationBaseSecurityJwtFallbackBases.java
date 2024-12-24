package jbst.iam.configurations;

import jbst.foundation.domain.properties.JbstProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        // -------------------------------------------------------------------------------------------------------------
        "jbst.iam.assistants.current.base",
        "jbst.iam.filters.jwt_extension",
        "jbst.iam.tasks.superadmin"
        // -------------------------------------------------------------------------------------------------------------
})
@EnableConfigurationProperties({
        JbstProperties.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationBaseSecurityJwtFallbackBases {

}
