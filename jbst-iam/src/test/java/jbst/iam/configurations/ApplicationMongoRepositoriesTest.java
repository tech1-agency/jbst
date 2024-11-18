package jbst.iam.configurations;

import jbst.iam.repositories.mongodb.MongoInvitationCodesRepository;
import jbst.iam.repositories.mongodb.MongoUsersRepository;
import jbst.iam.repositories.mongodb.MongoUsersSessionsRepository;
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
import tech1.framework.foundation.domain.properties.ApplicationFrameworkProperties;
import tech1.framework.foundation.domain.properties.ApplicationFrameworkPropertiesTestsHardcodedContext;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ApplicationMongoRepositoriesTest {

    @Configuration
    @Import({
            ApplicationFrameworkPropertiesTestsHardcodedContext.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {

        private final ApplicationFrameworkProperties applicationFrameworkProperties;

        @Bean
        MongoInvitationCodesRepository invitationCodeRepository() {
            return mock(MongoInvitationCodesRepository.class);
        }

        @Bean
        MongoUsersRepository usersRepository() {
            return mock(MongoUsersRepository.class);
        }

        @Bean
        MongoUsersSessionsRepository usersSessionsRepository() {
            return mock(MongoUsersSessionsRepository.class);
        }

        @Bean
        ApplicationMongoRepositories applicationMongoRepositories() {
            return new ApplicationMongoRepositories(
                    this.applicationFrameworkProperties
            );
        }
    }

    private final ApplicationMongoRepositories componentUnderTest;

    @Test
    void beansTests() {
        // Act
        var methods = Stream.of(this.componentUnderTest.getClass().getMethods())
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(methods)
                .hasSize(13)
                .contains("tech1MongoRepositories")
                .contains("tech1MongoClient")
                .contains("tech1MongoDatabaseFactory")
                .contains("tech1MongoTemplate");
        assertThat(this.componentUnderTest.tech1MongoClient()).isNotNull();
        assertThat(this.componentUnderTest.tech1MongoDatabaseFactory()).isNotNull();
        assertThat(this.componentUnderTest.tech1MongoTemplate()).isNotNull();
    }
}
