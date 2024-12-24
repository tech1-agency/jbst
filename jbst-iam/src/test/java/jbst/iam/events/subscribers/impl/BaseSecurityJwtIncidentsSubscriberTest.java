package jbst.iam.events.subscribers.impl;

import jbst.foundation.incidents.domain.authetication.*;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0Failure;
import jbst.iam.events.subscribers.incidents.SecurityJwtIncidentsSubscriber;
import jbst.iam.events.subscribers.incidents.base.BaseSecurityJwtIncidentsSubscriber;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1Failure;
import jbst.foundation.incidents.domain.session.IncidentSessionExpired;
import jbst.foundation.incidents.domain.session.IncidentSessionRefreshed;
import jbst.foundation.incidents.feigns.clients.IncidentClient;

import static org.mockito.Mockito.*;
import static jbst.foundation.utilities.random.EntityUtility.entity;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseSecurityJwtIncidentsSubscriberTest {

    @Configuration
    static class ContextConfiguration {
        @Bean
        IncidentClient incidentClient() {
            return mock(IncidentClient.class);
        }

        @Bean
        SecurityJwtIncidentsSubscriber securityJwtIncidentSubscriber() {
            return new BaseSecurityJwtIncidentsSubscriber(
                    this.incidentClient()
            );
        }
    }

    // Clients
    private final IncidentClient incidentClient;

    private final SecurityJwtIncidentsSubscriber componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.incidentClient
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.incidentClient
        );
    }


    @Test
    void onEventAuthenticationLoginIncidentTest() {
        // Arrange
        var incident = entity(IncidentAuthenticationLogin.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventAuthenticationLoginFailureUsernamePasswordIncidentTest() {
        // Arrange
        var incident = entity(IncidentAuthenticationLoginFailureUsernamePassword.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventAuthenticationLoginFailureUsernameMaskedPasswordIncidentTest() {
        // Arrange
        var incident = entity(IncidentAuthenticationLoginFailureUsernameMaskedPassword.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventAuthenticationLogoutMinIncidentTest() {
        // Arrange
        var incident = entity(IncidentAuthenticationLogoutMin.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventAuthenticationLogoutFullIncidentTest() {
        // Arrange
        var incident = entity(IncidentAuthenticationLogoutFull.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventSessionRefreshedIncidentTest() {
        // Arrange
        var incident = entity(IncidentSessionRefreshed.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventSessionExpiredIncidentTest() {
        // Arrange
        var incident = entity(IncidentSessionExpired.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventRegister0IncidentTest() {
        // Arrange
        var incident = entity(IncidentRegistration0.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventRegister0FailureIncidentTest() {
        // Arrange
        var incident = entity(IncidentRegistration0Failure.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventRegister1IncidentTest() {
        // Arrange
        var incident = entity(IncidentRegistration1.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }

    @Test
    void onEventRegister1FailureIncidentTest() {
        // Arrange
        var incident = entity(IncidentRegistration1Failure.class);

        // Act
        this.componentUnderTest.onEvent(incident);

        // Assert
        verify(this.incidentClient).registerIncident(incident.getPlainIncident());
    }
}
