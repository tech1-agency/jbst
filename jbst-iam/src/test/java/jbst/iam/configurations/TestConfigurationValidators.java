package jbst.iam.configurations;

import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.InvitationsRepository;
import jbst.iam.repositories.UsersTokensRepository;
import jbst.iam.repositories.UsersRepository;
import jbst.iam.repositories.UsersSessionsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;

import static org.mockito.Mockito.mock;

@Configuration
@Import({
        TestConfigurationPropertiesJbstHardcoded.class
})
public class TestConfigurationValidators {

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
    InvitationsRepository invitationsRepository() {
        return mock(InvitationsRepository.class);
    }

    @Bean
    UsersRepository userRepository() {
        return mock(UsersRepository.class);
    }

    @Bean
    UsersSessionsRepository userSessionRepository() {
        return mock(UsersSessionsRepository.class);
    }

    @Bean
    UsersTokensRepository usersTokensRepository() {
        return mock(UsersTokensRepository.class);
    }
}
