package jbst.iam.configurations;

import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.postgres.PostgresInvitationsRepository;
import jbst.iam.repositories.postgres.PostgresUsersRepository;
import jbst.iam.repositories.postgres.PostgresUsersSessionsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan({
        "jbst.iam.validators.postgres"
})
@Import({
        TestConfigurationPropertiesJbstHardcoded.class
})
public class TestConfigurationPostgresValidators {

    // =================================================================================================================
    // Publishers
    // =================================================================================================================
    @Bean
    SecurityJwtEventsPublisher securityJwtPublisher() {
        return mock(SecurityJwtEventsPublisher.class);
    }

    @Bean
    SecurityJwtIncidentsPublisher securityJwtIncidentPublisher() {
        return mock(SecurityJwtIncidentsPublisher.class);
    }

    @Bean
    IncidentPublisher incidentPublisher() {
        return mock(IncidentPublisher.class);
    }

    // =================================================================================================================
    // Repositories
    // =================================================================================================================
    @Bean
    PostgresInvitationsRepository invitationsRepository() {
        return mock(PostgresInvitationsRepository.class);
    }

    @Bean
    PostgresUsersRepository userRepository() {
        return mock(PostgresUsersRepository.class);
    }

    @Bean
    PostgresUsersSessionsRepository userSessionRepository() {
        return mock(PostgresUsersSessionsRepository.class);
    }
}
