package jbst.iam.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.utils.HttpRequestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan({
        "jbst.iam.handlers"
})
public class TestConfigurationHandlers {

    @Bean
    SecurityJwtEventsPublisher securityJwtPublisher() {
        return mock(SecurityJwtEventsPublisher.class);
    }

    @Bean
    SecurityJwtIncidentsPublisher securityJwtIncidentPublisher() {
        return mock(SecurityJwtIncidentsPublisher.class);
    }

    @Bean
    HttpRequestUtils httpRequestUtility() {
        return mock(HttpRequestUtils.class);
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
