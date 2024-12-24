package jbst.iam.events.publishers.base;

import jbst.iam.domain.events.*;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.events.publishers.events.base.BaseSecurityJwtEventsPublisher;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.mockito.Mockito.*;
import static jbst.foundation.utilities.random.EntityUtility.entity;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseSecurityJwtEventsPublisherTest {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        ApplicationEventPublisher applicationEventPublisher() {
            return mock(ApplicationEventPublisher.class);
        }

        @Bean
        SecurityJwtEventsPublisher securityJwtPublisher() {
            return new BaseSecurityJwtEventsPublisher(
                    this.applicationEventPublisher()
            );
        }
    }

    private final ApplicationEventPublisher applicationEventPublisher;

    private final SecurityJwtEventsPublisher componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.applicationEventPublisher
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.applicationEventPublisher
        );
    }

    @Test
    void publishAuthenticationLoginTest() {
        // Arrange
        var event = entity(EventAuthenticationLogin.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogin(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishAuthenticationLoginFailureTest() {
        // Arrange
        var event = entity(EventAuthenticationLoginFailure.class);

        // Act
        this.componentUnderTest.publishAuthenticationLoginFailure(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishAuthenticationLogoutTest() {
        // Arrange
        var event = entity(EventAuthenticationLogout.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogout(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishRegistration0Test() {
        // Arrange
        var event = entity(EventRegistration0.class);

        // Act
        this.componentUnderTest.publishRegistration0(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishRegistration0FailureTest() {
        // Arrange
        var event = entity(EventRegistration0Failure.class);

        // Act
        this.componentUnderTest.publishRegistration0Failure(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishRegistration1Test() {
        // Arrange
        var event = entity(EventRegistration1.class);

        // Act
        this.componentUnderTest.publishRegistration1(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishRegistration1FailureTest() {
        // Arrange
        var event = entity(EventRegistration1Failure.class);

        // Act
        this.componentUnderTest.publishRegistration1Failure(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishSessionRefreshedTest() {
        // Arrange
        var event = entity(EventSessionRefreshed.class);

        // Act
        this.componentUnderTest.publishSessionRefreshed(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishSessionExpiredTest() {
        // Arrange
        var event = entity(EventSessionExpired.class);

        // Act
        this.componentUnderTest.publishSessionExpired(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishSessionUserRequestMetadataAddTest() {
        // Arrange
        var event = entity(EventSessionUserRequestMetadataAdd.class);

        // Act
        this.componentUnderTest.publishSessionUserRequestMetadataAdd(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }

    @Test
    void publishSessionUserRequestMetadataRenewTest() {
        // Arrange
        var event = entity(EventSessionUserRequestMetadataRenew.class);

        // Act
        this.componentUnderTest.publishSessionUserRequestMetadataRenew(event);

        // Assert
        verify(this.applicationEventPublisher).publishEvent(event);
    }
}
