package jbst.iam.configurations;

import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.configs.MvcConfigs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class TestConfigurationPropertiesMocked {

    @Bean
    JbstProperties jbstProperties() {
        var jbstProperties = mock(JbstProperties.class);
        when(jbstProperties.getMvcConfigs()).thenReturn(MvcConfigs.hardcoded());
        return jbstProperties;
    }
}
