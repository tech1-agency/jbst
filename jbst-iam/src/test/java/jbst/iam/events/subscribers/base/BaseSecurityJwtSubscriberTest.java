package jbst.iam.events.subscribers.base;

import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.events.*;
import jbst.iam.domain.functions.FunctionAuthenticationLoginEmail;
import jbst.iam.domain.functions.FunctionSessionRefreshedEmail;
import jbst.iam.events.publishers.SecurityJwtIncidentPublisher;
import jbst.iam.events.subscribers.SecurityJwtSubscriber;
import jbst.iam.services.BaseUsersSessionsService;
import jbst.iam.services.UsersEmailsService;
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
import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.base.UsernamePasswordCredentials;
import jbst.foundation.domain.http.requests.IPAddress;
import jbst.foundation.domain.http.requests.UserAgentHeader;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLogin;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLoginFailureUsernameMaskedPassword;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLoginFailureUsernamePassword;
import jbst.foundation.incidents.domain.session.IncidentSessionRefreshed;
import jbst.foundation.utils.UserMetadataUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static jbst.foundation.utilities.random.EntityUtility.entity;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseSecurityJwtSubscriberTest {

    @Configuration
    static class ContextConfiguration {
        @Bean
        SecurityJwtIncidentPublisher securityJwtIncidentPublisher() {
            return mock(SecurityJwtIncidentPublisher.class);
        }

        @Bean
        UsersEmailsService userEmailService() {
            return mock(UsersEmailsService.class);
        }

        @Bean
        BaseUsersSessionsService baseUsersSessionsService() {
            return mock(BaseUsersSessionsService.class);
        }

        @Bean
        UserMetadataUtils userMetadataUtils() {
            return mock(UserMetadataUtils.class);
        }

        @Bean
        SecurityJwtSubscriber securityJwtSubscriber() {
            return new BaseSecurityJwtSubscriber(
                    this.securityJwtIncidentPublisher(),
                    this.userEmailService(),
                    this.baseUsersSessionsService(),
                    this.userMetadataUtils()
            );
        }
    }

    // Publishers
    private final SecurityJwtIncidentPublisher securityJwtIncidentPublisher;
    // Services
    private final UsersEmailsService usersEmailsService;
    private final BaseUsersSessionsService baseUsersSessionsService;
    // Utils
    private final UserMetadataUtils userMetadataUtils;

    private final SecurityJwtSubscriber componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.securityJwtIncidentPublisher,
                this.usersEmailsService,
                this.baseUsersSessionsService,
                this.userMetadataUtils
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.securityJwtIncidentPublisher,
                this.usersEmailsService,
                this.baseUsersSessionsService,
                this.userMetadataUtils
        );
    }

    @Test
    void onAuthenticationLoginTest() {
        // Arrange
        var event = entity(EventAuthenticationLogin.class);

        // Act
        this.componentUnderTest.onAuthenticationLogin(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onAuthenticationLoginFailureTest() {
        // Arrange
        var event = EventAuthenticationLoginFailure.hardcoded();
        when(this.userMetadataUtils.getUserRequestMetadataProcessed(event.ipAddress(), event.userAgentHeader())).thenReturn(UserRequestMetadata.valid());

        // Act
        this.componentUnderTest.onAuthenticationLoginFailure(event);

        // Assert
        verify(this.userMetadataUtils).getUserRequestMetadataProcessed(event.ipAddress(), event.userAgentHeader());
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLoginFailureUsernamePassword(
                new IncidentAuthenticationLoginFailureUsernamePassword(
                        new UsernamePasswordCredentials(
                                event.username(),
                                event.password()
                        ),
                        UserRequestMetadata.valid()
                )
        );
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLoginFailureUsernameMaskedPassword(
                new IncidentAuthenticationLoginFailureUsernameMaskedPassword(
                        UsernamePasswordCredentials.mask5(
                                event.username(),
                                event.password()
                        ),
                        UserRequestMetadata.valid()
                )
        );
    }

    @Test
    void onAuthenticationLogoutTest() {
        // Arrange
        var event = entity(EventAuthenticationLogout.class);

        // Act
        this.componentUnderTest.onAuthenticationLogout(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onRegistration0Test() {
        // Arrange
        var event = entity(EventRegistration0.class);

        // Act
        this.componentUnderTest.onRegistration0(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onRegistration0FailureTest() {
        // Arrange
        var event = entity(EventRegistration0Failure.class);

        // Act
        this.componentUnderTest.onRegistration0Failure(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onRegistration1Test() {
        // Arrange
        var event = entity(EventRegistration1.class);

        // Act
        this.componentUnderTest.onRegistration1(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onRegistration1FailureTest() {
        // Arrange
        var event = entity(EventRegistration1Failure.class);

        // Act
        this.componentUnderTest.onRegistration1Failure(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onSessionRefreshedTest() {
        // Arrange
        var event = entity(EventSessionRefreshed.class);

        // Act
        this.componentUnderTest.onSessionRefreshed(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onSessionExpiredTest() {
        // Arrange
        var event = entity(EventSessionExpired.class);

        // Act
        this.componentUnderTest.onSessionExpired(event);

        // Assert
        assertThat(event).isNotNull();
    }

    @Test
    void onSessionUserRequestMetadataAddNotAuthenticationEndpointTest() {
        // Arrange
        var event = new EventSessionUserRequestMetadataAdd(
                Username.random(),
                Email.random(),
                entity(UserSession.class),
                IPAddress.random(),
                mock(UserAgentHeader.class),
                false,
                false
        );
        when(this.baseUsersSessionsService.saveUserRequestMetadata(event)).thenReturn(event.session());

        // Act
        this.componentUnderTest.onSessionUserRequestMetadataAdd(event);

        // Assert
        verify(this.baseUsersSessionsService).saveUserRequestMetadata(event);
    }

    @Test
    void onSessionUserRequestMetadataAddIsAuthenticationLoginEndpointTest() {
        // Arrange
        var event = new EventSessionUserRequestMetadataAdd(
                Username.random(),
                Email.random(),
                entity(UserSession.class),
                IPAddress.random(),
                mock(UserAgentHeader.class),
                true,
                false
        );
        when(this.baseUsersSessionsService.saveUserRequestMetadata(event)).thenReturn(event.session());

        // Act
        this.componentUnderTest.onSessionUserRequestMetadataAdd(event);

        // Assert
        verify(this.baseUsersSessionsService).saveUserRequestMetadata(event);
        verify(this.usersEmailsService).executeAuthenticationLogin(new FunctionAuthenticationLoginEmail(event.username(), event.email(), event.session().metadata()));
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogin(new IncidentAuthenticationLogin(event.username(), event.session().metadata()));
    }

    @Test
    void onSessionUserRequestMetadataAddIsAuthenticationRefreshTokenEndpointTest() {
        // Arrange
        var event = new EventSessionUserRequestMetadataAdd(
                Username.random(),
                Email.random(),
                entity(UserSession.class),
                IPAddress.random(),
                mock(UserAgentHeader.class),
                false,
                true
        );
        when(this.baseUsersSessionsService.saveUserRequestMetadata(event)).thenReturn(event.session());

        // Act
        this.componentUnderTest.onSessionUserRequestMetadataAdd(event);

        // Assert
        verify(this.baseUsersSessionsService).saveUserRequestMetadata(event);
        verify(this.usersEmailsService).executeSessionRefreshed(new FunctionSessionRefreshedEmail(event.username(), event.email(), event.session().metadata()));
        verify(this.securityJwtIncidentPublisher).publishSessionRefreshed(new IncidentSessionRefreshed(event.username(), event.session().metadata()));
    }

    @Test
    void onSessionUserRequestMetadataRenewTest() {
        // Arrange
        var event = entity(EventSessionUserRequestMetadataRenew.class);

        // Act
        this.componentUnderTest.onSessionUserRequestMetadataRenew(event);

        // Assert
        verify(this.baseUsersSessionsService).saveUserRequestMetadata(event);
    }
}
