package jbst.iam.events.publishers.impl;

import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.base.JbstIamIncidentType;
import jbst.foundation.domain.properties.configs.SecurityJwtConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.IncidentsConfigs;
import jbst.foundation.incidents.domain.authetication.*;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0Failure;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1Failure;
import jbst.foundation.incidents.domain.session.IncidentSessionExpired;
import jbst.foundation.incidents.domain.session.IncidentSessionRefreshed;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.incidents.impl.BaseSecurityJwtIncidentsPublisher;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jbst.foundation.domain.properties.base.JbstIamIncidentType.*;
import static jbst.foundation.utilities.random.EntityUtility.entity;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseSecurityJwtIncidentsPublisherTest {

    @Configuration
    static class ContextConfiguration {
        @Primary
        @Bean
        ApplicationEventPublisher applicationEventPublisher() {
            return mock(ApplicationEventPublisher.class);
        }

        @Bean
        JbstProperties jbstProperties() {
            return mock(JbstProperties.class);
        }

        @Bean
        SecurityJwtIncidentsPublisher securityJwtIncidentPublisher() {
            return new BaseSecurityJwtIncidentsPublisher(
                    this.applicationEventPublisher(),
                    this.jbstProperties()
            );
        }
    }

    // Spring Publisher
    private final ApplicationEventPublisher applicationEventPublisher;
    // Properties
    private final JbstProperties jbstProperties;

    private final SecurityJwtIncidentsPublisher componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.applicationEventPublisher,
                this.jbstProperties
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.applicationEventPublisher,
                this.jbstProperties
        );
    }

    @Test
    void publishAuthenticationLoginDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGIN, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLogin.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogin(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishAuthenticationLoginEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGIN, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLogin.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogin(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @Test
    void publishAuthenticationLoginFailureUsernamePasswordDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLoginFailureUsernamePassword.class);

        // Act
        this.componentUnderTest.publishAuthenticationLoginFailureUsernamePassword(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishAuthenticationLoginFailureUsernamePasswordEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLoginFailureUsernamePassword.class);

        // Act
        this.componentUnderTest.publishAuthenticationLoginFailureUsernamePassword(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @Test
    void publishAuthenticationLoginFailureUsernameMaskedPasswordDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLoginFailureUsernameMaskedPassword.class);

        // Act
        this.componentUnderTest.publishAuthenticationLoginFailureUsernameMaskedPassword(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishAuthenticationLoginFailureUsernameMaskedPasswordEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLoginFailureUsernameMaskedPassword.class);

        // Act
        this.componentUnderTest.publishAuthenticationLoginFailureUsernameMaskedPassword(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @Test
    void publishAuthenticationLogoutMinDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGOUT_MIN, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLogoutMin.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogoutMin(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishAuthenticationLogoutMinEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGOUT_MIN, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLogoutMin.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogoutMin(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @Test
    void publishAuthenticationLogoutFullDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGOUT, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLogoutFull.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogoutFull(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishAuthenticationLogoutFullEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(AUTHENTICATION_LOGOUT, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentAuthenticationLogoutFull.class);

        // Act
        this.componentUnderTest.publishAuthenticationLogoutFull(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @Test
    void publishSessionRefreshedDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(SESSION_REFRESHED, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentSessionRefreshed.class);

        // Act
        this.componentUnderTest.publishSessionRefreshed(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishSessionRefreshedEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(SESSION_REFRESHED, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentSessionRefreshed.class);

        // Act
        this.componentUnderTest.publishSessionRefreshed(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @Test
    void publishSessionExpiredDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(SESSION_EXPIRED, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentSessionExpired.class);

        // Act
        this.componentUnderTest.publishSessionExpired(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishSessionExpiredEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(SESSION_EXPIRED, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentSessionExpired.class);

        // Act
        this.componentUnderTest.publishSessionExpired(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void publishRegistration0Test(boolean enabled) {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(REGISTER0, enabled);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentRegistration0.class);

        // Act
        this.componentUnderTest.publishRegistration0(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        if (enabled) {
            verify(this.applicationEventPublisher).publishEvent(incident);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void publishRegistration0FailureTest(boolean enabled) {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(REGISTER0_FAILURE, enabled);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentRegistration0Failure.class);

        // Act
        this.componentUnderTest.publishRegistration0Failure(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        if (enabled) {
            verify(this.applicationEventPublisher).publishEvent(incident);
        }
    }

    @Test
    void publishRegistration1DisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(REGISTER1, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentRegistration1.class);

        // Act
        this.componentUnderTest.publishRegistration1(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishRegistration1EnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(REGISTER1, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentRegistration1.class);

        // Act
        this.componentUnderTest.publishRegistration1(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    @Test
    void publishRegistration1FailureDisabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(REGISTER1_FAILURE, false);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentRegistration1Failure.class);

        // Act
        this.componentUnderTest.publishRegistration1Failure(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void publishRegistration1FailureEnabledTest() {
        // Arrange
        var securityJwtConfigs = randomSecurityJwtConfigs(REGISTER1_FAILURE, true);
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);
        var incident = entity(IncidentRegistration1Failure.class);

        // Act
        this.componentUnderTest.publishRegistration1Failure(incident);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.applicationEventPublisher).publishEvent(incident);
    }

    // =================================================================================================================
    // PRIVATE METHODS
    // =================================================================================================================
    private static SecurityJwtConfigs randomSecurityJwtConfigs(JbstIamIncidentType type, boolean enabled) {
        var typesConfigs = Stream.of(JbstIamIncidentType.values())
                .collect(Collectors.toMap(
                        entry -> entry,
                        entry -> type.equals(entry) && enabled
                ));
        return new SecurityJwtConfigs(
                null,
                null,
                null,
                new IncidentsConfigs(
                        typesConfigs
                ),
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
