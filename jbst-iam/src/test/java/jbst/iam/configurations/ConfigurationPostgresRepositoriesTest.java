package jbst.iam.configurations;

import jbst.iam.repositories.postgres.PostgresInvitationsRepository;
import jbst.iam.repositories.postgres.PostgresUsersTokensRepository;
import jbst.iam.repositories.postgres.PostgresUsersRepository;
import jbst.iam.repositories.postgres.PostgresUsersSessionsRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ConfigurationPostgresRepositoriesTest {

    @Configuration
    @Import({
            TestConfigurationPropertiesJbstHardcoded.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {

        @Bean
        PostgresInvitationsRepository invitationsRepository() {
            return mock(PostgresInvitationsRepository.class);
        }

        @Bean
        PostgresUsersTokensRepository usersTokensRepository() {
            return mock(PostgresUsersTokensRepository.class);
        }

        @Bean
        PostgresUsersRepository usersRepository() {
            return mock(PostgresUsersRepository.class);
        }

        @Bean
        PostgresUsersSessionsRepository usersSessionsRepository() {
            return mock(PostgresUsersSessionsRepository.class);
        }

        @Bean
        ConfigurationPostgresRepositories applicationPostgresRepositories() {
            return new ConfigurationPostgresRepositories(
                    this.invitationsRepository(),
                    this.usersTokensRepository(),
                    this.usersRepository(),
                    this.usersSessionsRepository()
            );
        }
    }

    private final ConfigurationPostgresRepositories componentUnderTest;

    @Test
    void beansTests() {
        // Act
        var methods = Stream.of(this.componentUnderTest.getClass().getMethods())
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(methods)
                .hasSize(10)
                .contains("jbstPostgresRepositories");
    }
}
