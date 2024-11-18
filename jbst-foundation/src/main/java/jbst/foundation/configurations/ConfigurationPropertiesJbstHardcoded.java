package jbst.foundation.configurations;

import jbst.foundation.domain.properties.JbstProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationPropertiesJbstHardcoded {

    @Bean
    public JbstProperties jbstProperties() {
        return JbstProperties.hardcoded();
    }
}
