package jbst.iam.events.subscribers.base;

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
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.foundation.utils.UserMetadataUtils;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.db.UserToken;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.events.*;
import jbst.iam.domain.functions.FunctionAccountAccessed;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.subscribers.events.SecurityJwtEventsSubscriber;
import jbst.iam.events.subscribers.events.base.BaseSecurityJwtEventsSubscriber;
import jbst.iam.services.BaseUsersSessionsService;
import jbst.iam.services.BaseUsersTokensService;
import jbst.iam.services.UsersEmailsService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static jbst.foundation.utilities.random.EntityUtility.entity;
import static jbst.iam.domain.enums.AccountAccessMethod.SECURITY_TOKEN;
import static jbst.iam.domain.enums.AccountAccessMethod.USERNAME_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseSecurityJwtEventsSubscriberTest {

    private static Stream<Arguments> exceptionalExecutionParams() {
        return Stream.of(
                Arguments.of(new IllegalArgumentException()),
                Arguments.of((Object) null)
        );
    }

    private static Stream<Arguments> eventSessionUserRequestMetadataAddLoginTest() {
        return Stream.of(
                Arguments.of(
                        new EventSessionUserRequestMetadataAdd(
                                Username.random(),
                                Email.random(),
                                entity(UserSession.class),
                                IPAddress.random(),
                                mock(UserAgentHeader.class),
                                true,
                                false
                        ),
                        null
                ),
                Arguments.of(
                        new EventSessionUserRequestMetadataAdd(
                                Username.random(),
                                null,
                                entity(UserSession.class),
                                IPAddress.random(),
                                mock(UserAgentHeader.class),
                                true,
                                false
                        ),
                        null
                ),
                Arguments.of(
                        new EventSessionUserRequestMetadataAdd(
                                Username.random(),
                                null,
                                entity(UserSession.class),
                                IPAddress.random(),
                                mock(UserAgentHeader.class),
                                true,
                                false
                        ),
                        new RuntimeException("Unexpected error occurred")
                )
        );
    }

    private static Stream<Arguments> eventSessionUserRequestMetadataAddRefreshTest() {
        return Stream.of(
                Arguments.of(
                        new EventSessionUserRequestMetadataAdd(
                                Username.random(),
                                Email.random(),
                                entity(UserSession.class),
                                IPAddress.random(),
                                mock(UserAgentHeader.class),
                                false,
                                true
                        ),
                        null
                ),
                Arguments.of(
                        new EventSessionUserRequestMetadataAdd(
                                Username.random(),
                                null,
                                entity(UserSession.class),
                                IPAddress.random(),
                                mock(UserAgentHeader.class),
                                false,
                                true
                        ),
                        null
                ),
                Arguments.of(
                        new EventSessionUserRequestMetadataAdd(
                                Username.random(),
                                null,
                                entity(UserSession.class),
                                IPAddress.random(),
                                mock(UserAgentHeader.class),
                                false,
                                true
                        ),
                        new RuntimeException("Unexpected error occurred")
                )
        );
    }

    public static Stream<Arguments> onSessionUserRequestMetadataRenewTest() {
        return Stream.of(
                Arguments.of((RuntimeException) null),
                Arguments.of(new RuntimeException("Unexpected error occurred"))
        );
    }

    @Configuration
    static class ContextConfiguration {
        @Bean
        SecurityJwtIncidentsPublisher securityJwtIncidentPublisher() {
            return mock(SecurityJwtIncidentsPublisher.class);
        }

        @Bean
        BaseUsersTokensService baseUsersTokensService() {
            return mock(BaseUsersTokensService.class);
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
        IncidentPublisher incidentPublisher() {
            return mock(IncidentPublisher.class);
        }

        @Bean
        SecurityJwtEventsSubscriber securityJwtSubscriber() {
            return new BaseSecurityJwtEventsSubscriber(
                    this.securityJwtIncidentPublisher(),
                    this.baseUsersTokensService(),
                    this.userEmailService(),
                    this.baseUsersSessionsService(),
                    this.userMetadataUtils(),
                    this.incidentPublisher()
            );
        }
    }

    // Publishers
    private final SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher;
    // Services
    private final BaseUsersTokensService baseUsersTokensService;
    private final UsersEmailsService usersEmailsService;
    private final BaseUsersSessionsService baseUsersSessionsService;
    // Utils
    private final UserMetadataUtils userMetadataUtils;
    // Incidents
    private final IncidentPublisher incidentPublisher;

    private final SecurityJwtEventsSubscriber componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.securityJwtIncidentsPublisher,
                this.baseUsersTokensService,
                this.usersEmailsService,
                this.baseUsersSessionsService,
                this.userMetadataUtils,
                this.incidentPublisher
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.securityJwtIncidentsPublisher,
                this.baseUsersTokensService,
                this.usersEmailsService,
                this.baseUsersSessionsService,
                this.userMetadataUtils,
                this.incidentPublisher
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

    @ParameterizedTest
    @MethodSource("exceptionalExecutionParams")
    void onAuthenticationLoginFailureTest(RuntimeException ex) {
        // Arrange
        var event = EventAuthenticationLoginFailure.hardcoded();
        when(this.userMetadataUtils.getUserRequestMetadataProcessed(event.ipAddress(), event.userAgentHeader())).thenReturn(UserRequestMetadata.valid());
        if (nonNull(ex)) {
            doThrow(ex).when(this.securityJwtIncidentsPublisher).publishAuthenticationLoginFailureUsernameMaskedPassword(any());
        }

        // Act
        this.componentUnderTest.onAuthenticationLoginFailure(event);

        // Assert
        verify(this.userMetadataUtils).getUserRequestMetadataProcessed(event.ipAddress(), event.userAgentHeader());
        verify(this.securityJwtIncidentsPublisher).publishAuthenticationLoginFailureUsernamePassword(
                new IncidentAuthenticationLoginFailureUsernamePassword(
                        new UsernamePasswordCredentials(
                                event.username(),
                                event.password()
                        ),
                        UserRequestMetadata.valid()
                )
        );
        verify(this.securityJwtIncidentsPublisher).publishAuthenticationLoginFailureUsernameMaskedPassword(
                new IncidentAuthenticationLoginFailureUsernameMaskedPassword(
                        UsernamePasswordCredentials.mask5(
                                event.username(),
                                event.password()
                        ),
                        UserRequestMetadata.valid()
                )
        );
        verify(this.incidentPublisher, nonNull(ex) ? times(1) : times(0)).publishThrowable(ex);
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

    @ParameterizedTest
    @MethodSource("exceptionalExecutionParams")
    void onRegistration0Test(RuntimeException ex) {
        // Arrange
        var requestUserRegistration0 = RequestUserRegistration0.hardcoded();
        var event = new EventRegistration0(requestUserRegistration0);
        var userToken = UserToken.hardcoded();
        when(this.baseUsersTokensService.saveAs(requestUserRegistration0.asRequestUserEmailConfirmationToken())).thenReturn(userToken);
        var functionConfirmEmail = userToken.asFunctionEmailConfirmation(requestUserRegistration0.email());
        if (nonNull(ex)) {
            doThrow(ex).when(this.usersEmailsService).executeEmailConfirmation(functionConfirmEmail);
        }

        // Act
        this.componentUnderTest.onRegistration0(event);

        // Assert
        assertThat(event).isNotNull();
        verify(this.baseUsersTokensService).saveAs(requestUserRegistration0.asRequestUserEmailConfirmationToken());
        verify(this.usersEmailsService).executeEmailConfirmation(functionConfirmEmail);
        verify(this.incidentPublisher, nonNull(ex) ? times(1) : times(0)).publishThrowable(ex);
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

    @ParameterizedTest
    @MethodSource("eventSessionUserRequestMetadataAddLoginTest")
    void onSessionUserRequestMetadataAddIsAuthenticationLoginEndpointTest(
            EventSessionUserRequestMetadataAdd event,
            RuntimeException ex
    ) {
        // Arrange
        when(this.baseUsersSessionsService.saveUserRequestMetadata(event)).thenReturn(event.session());
        if (nonNull(ex)) {
            doThrow(ex).when(this.securityJwtIncidentsPublisher).publishAuthenticationLogin(any());
        }

        // Act
        this.componentUnderTest.onSessionUserRequestMetadataAdd(event);

        // Assert
        verify(this.baseUsersSessionsService).saveUserRequestMetadata(event);
        if (nonNull(event.email())) {
            verify(this.usersEmailsService).executeAuthenticationLogin(new FunctionAccountAccessed(event.username(), event.email(), event.session().metadata(), USERNAME_PASSWORD));
        } else {
            verifyNoInteractions(this.usersEmailsService);
        }
        verify(this.securityJwtIncidentsPublisher).publishAuthenticationLogin(new IncidentAuthenticationLogin(event.username(), event.session().metadata()));
        verify(this.incidentPublisher, nonNull(ex) ? times(1) : times(0)).publishThrowable(ex);
    }

    @ParameterizedTest
    @MethodSource("eventSessionUserRequestMetadataAddRefreshTest")
    void onSessionUserRequestMetadataAddIsAuthenticationRefreshTokenEndpointTest(
            EventSessionUserRequestMetadataAdd event,
            RuntimeException ex
    ) {
        // Arrange
        when(this.baseUsersSessionsService.saveUserRequestMetadata(event)).thenReturn(event.session());
        if (nonNull(ex)) {
            doThrow(ex).when(this.securityJwtIncidentsPublisher).publishSessionRefreshed(any());
        }

        // Act
        this.componentUnderTest.onSessionUserRequestMetadataAdd(event);

        // Assert
        verify(this.baseUsersSessionsService).saveUserRequestMetadata(event);
        if (nonNull(event.email())) {
            verify(this.usersEmailsService).executeSessionRefreshed(new FunctionAccountAccessed(event.username(), event.email(), event.session().metadata(), SECURITY_TOKEN));
        } else {
            verifyNoInteractions(this.usersEmailsService);
        }
        verify(this.securityJwtIncidentsPublisher).publishSessionRefreshed(new IncidentSessionRefreshed(event.username(), event.session().metadata()));
        verify(this.incidentPublisher, nonNull(ex) ? times(1) : times(0)).publishThrowable(ex);
    }

    @ParameterizedTest
    @MethodSource("onSessionUserRequestMetadataRenewTest")
    void onSessionUserRequestMetadataRenewTest(RuntimeException ex) {
        // Arrange
        var event = entity(EventSessionUserRequestMetadataRenew.class);
        if (nonNull(ex)) {
            doThrow(ex).when(this.baseUsersSessionsService).saveUserRequestMetadata(event);
        }

        // Act
        this.componentUnderTest.onSessionUserRequestMetadataRenew(event);

        // Assert
        verify(this.baseUsersSessionsService).saveUserRequestMetadata(event);
        verify(this.incidentPublisher, nonNull(ex) ? times(1) : times(0)).publishThrowable(ex);
    }
}
