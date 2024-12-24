package jbst.iam.services.abstracts;

import jakarta.servlet.http.HttpServletRequest;
import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.constants.JbstConstants;
import jbst.foundation.domain.enums.Status;
import jbst.foundation.domain.http.requests.IPAddress;
import jbst.foundation.domain.http.requests.UserAgentHeader;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.tests.constants.TestsFlagsConstants;
import jbst.foundation.domain.tuples.TuplePresence;
import jbst.foundation.domain.tuples.TupleToggle;
import jbst.foundation.utils.UserMetadataUtils;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.events.EventSessionUserRequestMetadataAdd;
import jbst.iam.domain.events.EventSessionUserRequestMetadataRenew;
import jbst.iam.domain.functions.FunctionSessionUserRequestMetadataSave;
import jbst.iam.domain.identifiers.UserSessionId;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.UsersSessionsRepository;
import jbst.iam.utils.SecurityJwtTokenUtils;
import jbst.iam.utils.impl.SecurityJwtTokenUtilsImpl;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static jbst.foundation.domain.tuples.TuplePresence.present;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.entityAccessDenied;
import static jbst.foundation.utilities.http.HttpServletRequestUtility.getClientIpAddr;
import static jbst.foundation.utilities.random.EntityUtility.entity;
import static jbst.foundation.utilities.random.RandomUtility.randomIPv4;
import static jbst.foundation.utilities.random.RandomUtility.randomString;
import static jbst.foundation.utilities.time.TimestampUtility.getCurrentTimestamp;
import static jbst.iam.domain.db.UserSession.randomPersistedSession;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AbstractBaseUsersSessionsServiceTest {

    private static Stream<Arguments> saveUserRequestMetadataTest() {
        return Stream.of(
                Arguments.of(TupleToggle.disabled(), TupleToggle.disabled(), false, false),
                Arguments.of(TupleToggle.disabled(), TupleToggle.enabled(true), false, true),
                Arguments.of(TupleToggle.disabled(), TupleToggle.enabled(false), false, false),
                Arguments.of(TupleToggle.enabled(true), TupleToggle.disabled(), true, false),
                Arguments.of(TupleToggle.enabled(false), TupleToggle.disabled(), false, false),
                Arguments.of(TupleToggle.enabled(false), TupleToggle.enabled(false), false, false),
                Arguments.of(TupleToggle.enabled(false), TupleToggle.enabled(true), false, true),
                Arguments.of(TupleToggle.enabled(true), TupleToggle.enabled(false), true, false),
                Arguments.of(TupleToggle.enabled(true), TupleToggle.enabled(true), true, true)
        );
    }

    private static Stream<Arguments> renewUserRequestMetadataArgs() {
        return Stream.of(
                Arguments.of(false, false, TupleToggle.disabled(), TupleToggle.disabled()),
                Arguments.of(true, false, TupleToggle.enabled(true), TupleToggle.disabled()),
                Arguments.of(false, true, TupleToggle.disabled(), TupleToggle.enabled(true)),
                Arguments.of(true, true, TupleToggle.enabled(true), TupleToggle.enabled(true))
        );
    }

    @Configuration
    @Import({
            TestConfigurationPropertiesJbstHardcoded.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        private final JbstProperties jbstProperties;

        @Bean
        SecurityJwtEventsPublisher securityJwtPublisher() {
            return mock(SecurityJwtEventsPublisher.class);
        }

        @Bean
        UsersSessionsRepository usersSessionsRepository() {
            return mock(UsersSessionsRepository.class);
        }

        @Bean
        public SecurityJwtTokenUtils securityJwtTokenUtils() {
            return new SecurityJwtTokenUtilsImpl(
                    this.jbstProperties
            );
        }

        @Bean
        UserMetadataUtils userMetadataUtils() {
            return mock(UserMetadataUtils.class);
        }

        @Bean
        AbstractBaseUsersSessionsService abstractTokensContextThrowerService() {
            return new AbstractBaseUsersSessionsService(
                    this.securityJwtPublisher(),
                    this.usersSessionsRepository(),
                    this.userMetadataUtils(),
                    this.securityJwtTokenUtils()
            ) {};
        }
    }

    // Publishers
    private final SecurityJwtEventsPublisher securityJwtEventsPublisher;
    // Repositories
    private final UsersSessionsRepository usersSessionsRepository;
    // Utils
    private final UserMetadataUtils userMetadataUtils;

    private final AbstractBaseUsersSessionsService componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.securityJwtEventsPublisher,
                this.usersSessionsRepository,
                this.userMetadataUtils
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.securityJwtEventsPublisher,
                this.usersSessionsRepository,
                this.userMetadataUtils
        );
    }

    @Test
    void assertAccess() {
        when(this.usersSessionsRepository.isPresent(UserSessionId.hardcoded(), Username.hardcoded())).thenReturn(TuplePresence.present(UserSession.randomPersistedSession()));

        // Act
        this.componentUnderTest.assertAccess(Username.hardcoded(), UserSessionId.hardcoded());

        // Assert
        verify(this.usersSessionsRepository).isPresent(UserSessionId.hardcoded(), Username.hardcoded());
    }

    @Test
    void assertAccessNoAccess() {
        when(this.usersSessionsRepository.isPresent(UserSessionId.hardcoded(), Username.hardcoded())).thenReturn(TuplePresence.absent());

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.assertAccess(Username.hardcoded(), UserSessionId.hardcoded()));

        // Assert
        verify(this.usersSessionsRepository).isPresent(UserSessionId.hardcoded(), Username.hardcoded());
        assertThat(throwable)
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(entityAccessDenied("Session", UserSessionId.hardcoded().value()));
    }

    @Test
    void saveUserSessionNotNullTest() {
        // Arrange
        var ipAddr = randomIPv4();
        var httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(randomString());
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(ipAddr);
        var user = entity(JwtUser.class);
        var username = user.username();
        var accessToken = JwtAccessToken.random();
        var refreshToken = JwtRefreshToken.random();
        var userSession = UserSession.random(username, accessToken, refreshToken);
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(present(userSession));
        when(this.usersSessionsRepository.saveAs(any(UserSession.class))).thenReturn(userSession);

        // Act
        this.componentUnderTest.save(user, accessToken, refreshToken, httpServletRequest);

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
        var dbUserSessionAC = ArgumentCaptor.forClass(UserSession.class);
        verify(this.usersSessionsRepository).saveAs(dbUserSessionAC.capture());
        var actualDbUserSession = dbUserSessionAC.getValue();
        assertThat(actualDbUserSession.username()).isEqualTo(username);
        assertThat(actualDbUserSession.refreshToken()).isEqualTo(refreshToken);
        var requestMetadata = actualDbUserSession.metadata();
        assertThat(requestMetadata.getStatus()).isEqualTo(Status.STARTED);
        assertThat(requestMetadata.getGeoLocation().getIpAddr()).isEqualTo(ipAddr);
        var whereTuple3 = requestMetadata.getWhereTuple3();
        assertThat(whereTuple3.a()).isEqualTo(ipAddr);
        assertThat(whereTuple3.b()).isEqualTo(TestsFlagsConstants.UNKNOWN);
        assertThat(whereTuple3.c()).isEqualTo("Processing. Please wait...");
        var whatTuple2 = requestMetadata.getWhatTuple2();
        assertThat(whatTuple2.a()).isEqualTo(JbstConstants.Strings.UNDEFINED);
        assertThat(whatTuple2.b()).isEqualTo("—");
        assertThat(actualDbUserSession.id()).isNotNull();
        var eventAC = ArgumentCaptor.forClass(EventSessionUserRequestMetadataAdd.class);
        verify(this.securityJwtEventsPublisher).publishSessionUserRequestMetadataAdd(eventAC.capture());
        var event = eventAC.getValue();
        assertThat(event.username()).isEqualTo(username);
        assertThat(event.session().id()).isEqualTo(actualDbUserSession.id());
        assertThat(event.session().metadata()).isNotEqualTo(actualDbUserSession.metadata());
        assertThat(event.isAuthenticationLoginEndpoint()).isTrue();
        assertThat(event.isAuthenticationRefreshTokenEndpoint()).isFalse();
    }

    @Test
    void saveUserSessionNullTest() {
        // Arrange
        var ipAddr = randomIPv4();
        var httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(randomString());
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(ipAddr);
        var user = entity(JwtUser.class);
        var username = user.username();
        var accessToken = JwtAccessToken.random();
        var refreshToken = JwtRefreshToken.random();
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(TuplePresence.absent());
        var userSession = UserSession.random(username, accessToken, refreshToken);
        when(this.usersSessionsRepository.saveAs(any(UserSession.class))).thenReturn(userSession);

        // Act
        this.componentUnderTest.save(user, accessToken, refreshToken, httpServletRequest);

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
        var dbUserSessionAC = ArgumentCaptor.forClass(UserSession.class);
        verify(this.usersSessionsRepository).saveAs(dbUserSessionAC.capture());
        var actualDbUserSession = dbUserSessionAC.getValue();
        assertThat(actualDbUserSession.username()).isEqualTo(username);
        assertThat(actualDbUserSession.refreshToken()).isEqualTo(refreshToken);
        var requestMetadata = actualDbUserSession.metadata();
        assertThat(requestMetadata.getStatus()).isEqualTo(Status.STARTED);
        assertThat(requestMetadata.getGeoLocation().getIpAddr()).isEqualTo(ipAddr);
        var whereTuple3 = requestMetadata.getWhereTuple3();
        assertThat(whereTuple3.a()).isEqualTo(ipAddr);
        assertThat(whereTuple3.b()).isEqualTo(TestsFlagsConstants.UNKNOWN);
        assertThat(whereTuple3.c()).isEqualTo("Processing. Please wait...");
        var whatTuple2 = requestMetadata.getWhatTuple2();
        assertThat(whatTuple2.a()).isEqualTo(JbstConstants.Strings.UNDEFINED);
        assertThat(whatTuple2.b()).isEqualTo("—");
        assertThat(actualDbUserSession.id()).isNotNull();
        var eventAC = ArgumentCaptor.forClass(EventSessionUserRequestMetadataAdd.class);
        verify(this.securityJwtEventsPublisher).publishSessionUserRequestMetadataAdd(eventAC.capture());
        var event = eventAC.getValue();
        assertThat(event.username()).isEqualTo(username);
        assertThat(event.session().id()).isNotEqualTo(actualDbUserSession.id());
        assertThat(event.session().metadata()).isNotEqualTo(actualDbUserSession.metadata());
        assertThat(event.isAuthenticationLoginEndpoint()).isTrue();
        assertThat(event.isAuthenticationRefreshTokenEndpoint()).isFalse();
    }

    @Test
    void refreshTest() {
        // Arrange
        var httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(randomString());
        var user = entity(JwtUser.class);
        var username = user.username();
        var newAccessToken = JwtAccessToken.random();
        var newRefreshToken = JwtRefreshToken.random();
        var oldSession = randomPersistedSession();
        when(this.usersSessionsRepository.saveAs(any(UserSession.class))).thenReturn(randomPersistedSession());

        // Act
        this.componentUnderTest.refresh(user, oldSession, newAccessToken, newRefreshToken, httpServletRequest);

        // Assert
        var saveCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(this.usersSessionsRepository).saveAs(saveCaptor.capture());
        var newUserSession = saveCaptor.getValue();
        assertThat(newUserSession.username()).isEqualTo(username);
        assertThat(newUserSession.refreshToken()).isEqualTo(newRefreshToken);
        assertThat(newUserSession.metadata()).isEqualTo(oldSession.metadata());
        verify(this.usersSessionsRepository).delete(oldSession.id());
        var eventAC = ArgumentCaptor.forClass(EventSessionUserRequestMetadataAdd.class);
        verify(this.securityJwtEventsPublisher).publishSessionUserRequestMetadataAdd(eventAC.capture());
        var event = eventAC.getValue();
        assertThat(event.username()).isEqualTo(username);
        assertThat(event.email()).isEqualTo(user.email());
        assertThat(event.session().id()).isNotEqualTo(newUserSession.id());
        assertThat(event.isAuthenticationLoginEndpoint()).isFalse();
        assertThat(event.isAuthenticationRefreshTokenEndpoint()).isTrue();
    }

    @Test
    void saveUserRequestMetadataEventSessionUserRequestMetadataAddTest() {
        var event = entity(EventSessionUserRequestMetadataAdd.class);
        when(this.userMetadataUtils.getUserRequestMetadataProcessed(event.clientIpAddr(), event.userAgentHeader())).thenReturn(UserRequestMetadata.valid());
        when(this.usersSessionsRepository.saveAs(any(UserSession.class))).thenReturn(event.session());

        // Act
        this.componentUnderTest.saveUserRequestMetadata(event);

        // Assert
        verify(this.userMetadataUtils).getUserRequestMetadataProcessed(event.clientIpAddr(), event.userAgentHeader());
        var userSessionAC = ArgumentCaptor.forClass(UserSession.class);
        verify(this.usersSessionsRepository).saveAs(userSessionAC.capture());
        assertThat(userSessionAC.getValue().metadata()).isEqualTo(UserRequestMetadata.valid());
    }

    @Test
    void saveUserRequestMetadataEventSessionUserRequestMetadataRenewTest() {
        var event = new EventSessionUserRequestMetadataRenew(
                Username.random(),
                entity(UserSession.class),
                IPAddress.random(),
                entity(UserAgentHeader.class),
                TupleToggle.disabled(),
                TupleToggle.disabled()
        );
        when(this.userMetadataUtils.getUserRequestMetadataProcessed(event.clientIpAddr(), event.userAgentHeader())).thenReturn(UserRequestMetadata.valid());
        when(this.usersSessionsRepository.saveAs(any(UserSession.class))).thenReturn(event.session());

        // Act
        this.componentUnderTest.saveUserRequestMetadata(event);

        // Assert
        verify(this.userMetadataUtils).getUserRequestMetadataProcessed(event.clientIpAddr(), event.userAgentHeader());
        var userSessionAC = ArgumentCaptor.forClass(UserSession.class);
        verify(this.usersSessionsRepository).saveAs(userSessionAC.capture());
        assertThat(userSessionAC.getValue().metadata()).isEqualTo(UserRequestMetadata.valid());
    }

    @ParameterizedTest
    @MethodSource("saveUserRequestMetadataTest")
    void saveUserRequestMetadataTest(
            TupleToggle<Boolean> metadataRenewCron,
            TupleToggle<Boolean> metadataRenewManually,
            boolean expectedMetadataRenewCron,
            boolean expectedMetadataRenewManually
    ) {
        // Arrange
        var username = Username.random();
        var session = UserSession.ofPersisted(
                entity(UserSessionId.class),
                getCurrentTimestamp(),
                getCurrentTimestamp(),
                username,
                JwtAccessToken.random(),
                JwtRefreshToken.random(),
                UserRequestMetadata.random(),
                false,
                false
        );
        var saveFunction = new FunctionSessionUserRequestMetadataSave(
                username,
                session,
                entity(IPAddress.class),
                entity(UserAgentHeader.class),
                metadataRenewCron,
                metadataRenewManually
        );
        when(this.userMetadataUtils.getUserRequestMetadataProcessed(saveFunction.clientIpAddr(), saveFunction.userAgentHeader())).thenReturn(UserRequestMetadata.valid());
        when(this.usersSessionsRepository.saveAs(any(UserSession.class))).thenReturn(saveFunction.session());

        // Act
        this.componentUnderTest.saveUserRequestMetadata(saveFunction);

        // Assert
        verify(this.userMetadataUtils).getUserRequestMetadataProcessed(saveFunction.clientIpAddr(), saveFunction.userAgentHeader());
        var userSessionAC = ArgumentCaptor.forClass(UserSession.class);
        verify(this.usersSessionsRepository).saveAs(userSessionAC.capture());
        var sessionProcessedMetadata = userSessionAC.getValue();
        assertThat(sessionProcessedMetadata.metadataRenewCron()).isEqualTo(expectedMetadataRenewCron);
        assertThat(sessionProcessedMetadata.metadataRenewManually()).isEqualTo(expectedMetadataRenewManually);
        assertThat(userSessionAC.getValue().metadata()).isEqualTo(UserRequestMetadata.valid());
    }

    @Test
    void getExpiredSessionsTest() {
        // Arrange
        var usernames = new HashSet<>(Set.of(Username.hardcoded()));
        var sessionInvalidUserSession = UserSession.random(
                Username.random(),
                JwtAccessToken.random(),
                new JwtRefreshToken("<invalid>")
        );
        var sessionExpiredUserSession = UserSession.random(
                Username.random(),
                JwtAccessToken.random(),
                new JwtRefreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtdWx0aXVzZXI0MyIsImF1dGhvcml0aWVzIjpbeyJhdXRob3JpdHkiOiJhZG1pbiJ9LHsiYXV0aG9yaXR5IjoidXNlciJ9XSwiaWF0IjoxNjQyNzc0NTk3LCJleHAiOjE2NDI3NzQ2Mjd9.KUkURlpCWsh0VJFC4xrCOxr_dXNusRRjdjFb88Wb4Rw")
        );
        var sessionAliveUserSession = UserSession.random(
                Username.random(),
                JwtAccessToken.random(),
                new JwtRefreshToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtdWx0aXVzZXI0MyIsImF1dGhvcml0aWVzIjpbeyJhdXRob3JpdHkiOiJhZG1pbiJ9LHsiYXV0aG9yaXR5IjoidXNlciJ9XSwiaWF0IjoxNjQyNzc0Nzc4LCJleHAiOjQ3OTg0NDgzNzh9.06Ep_ri727dkMEVA3zptDb8tmFn1VJ1FIhjjbE-mxpw")
        );
        var usersSessions = List.of(sessionInvalidUserSession, sessionExpiredUserSession, sessionAliveUserSession);
        when(this.usersSessionsRepository.findByUsernameInAsAny(usernames)).thenReturn(usersSessions);

        // Act
        var sessionsValidatedTuple2 = this.componentUnderTest.getExpiredRefreshTokensSessions(usernames);

        // Assert
        verify(this.usersSessionsRepository).findByUsernameInAsAny(usernames);
        assertThat(sessionsValidatedTuple2).isNotNull();
        assertThat(sessionsValidatedTuple2.expiredOrInvalidSessionIds()).isNotNull();
        assertThat(sessionsValidatedTuple2.expiredOrInvalidSessionIds()).hasSize(2);
        assertThat(sessionsValidatedTuple2.expiredOrInvalidSessionIds()).containsExactlyInAnyOrder(
                sessionInvalidUserSession.id(),
                sessionExpiredUserSession.id()
        );
        assertThat(sessionsValidatedTuple2.expiredSessions()).isNotNull();
        assertThat(sessionsValidatedTuple2.expiredSessions()).hasSize(1);
        assertThat(sessionsValidatedTuple2.expiredSessions().get(0).a().value()).isEqualTo("multiuser43");
    }

    @Test
    void enableUserRequestMetadataRenewCronTest() {
        // Act
        this.componentUnderTest.enableUserRequestMetadataRenewCron();

        // Assert
        verify(this.usersSessionsRepository).enableMetadataRenewCron();
    }

    @Test
    void enableUserRequestMetadataRenewManuallyTest() {
        // Arrange
        var sessionId = entity(UserSessionId.class);

        // Act
        this.componentUnderTest.enableUserRequestMetadataRenewManually(sessionId);

        // Assert
        verify(this.usersSessionsRepository).enableMetadataRenewManually(sessionId);
    }

    @ParameterizedTest
    @MethodSource("renewUserRequestMetadataArgs")
    void renewUserRequestMetadataTest(
            boolean metadataRenewCron,
            boolean metadataRenewManually,
            TupleToggle<Boolean> expectedMetadataRenewCron,
            TupleToggle<Boolean> expectedMetadataRenewManually
    ) {
        // Arrange
        var httpServletRequest = mock(HttpServletRequest.class);
        var session = UserSession.ofPersisted(
                entity(UserSessionId.class),
                getCurrentTimestamp(),
                getCurrentTimestamp(),
                Username.random(),
                JwtAccessToken.random(),
                JwtRefreshToken.random(),
                UserRequestMetadata.random(),
                metadataRenewCron,
                metadataRenewManually
        );

        // Act
        this.componentUnderTest.renewUserRequestMetadata(session, httpServletRequest);

        // Assert
        var eventAC = ArgumentCaptor.forClass(EventSessionUserRequestMetadataRenew.class);
        if (session.isRenewRequired()) {
            verify(this.securityJwtEventsPublisher).publishSessionUserRequestMetadataRenew(eventAC.capture());
            var event = eventAC.getValue();
            assertThat(event.username()).isEqualTo(session.username());
            assertThat(event.session()).isEqualTo(session);
            assertThat(event.clientIpAddr()).isEqualTo(getClientIpAddr(httpServletRequest));
            assertThat(event.userAgentHeader()).isEqualTo(new UserAgentHeader(httpServletRequest));
            assertThat(event.metadataRenewCron()).isEqualTo(expectedMetadataRenewCron);
            assertThat(event.metadataRenewManually()).isEqualTo(expectedMetadataRenewManually);
        }
    }

    @Test
    void deleteByIdTest() {
        // Arrange
        var sessionId = entity(UserSessionId.class);

        // Act
        this.componentUnderTest.deleteById(sessionId);

        // Assert
        verify(this.usersSessionsRepository).delete(sessionId);
    }

    @Test
    void deleteAllExceptCurrentTest() {
        // Arrange
        var username = entity(Username.class);
        var requestAccessToken = RequestAccessToken.random();

        // Act
        this.componentUnderTest.deleteAllExceptCurrent(username, requestAccessToken);

        // Assert
        verify(this.usersSessionsRepository).deleteByUsernameExceptAccessToken(username, requestAccessToken);
    }

    @Test
    void deleteAllExceptCurrentAsSuperuserTest() {
        // Arrange
        var requestAccessToken = RequestAccessToken.random();

        // Act
        this.componentUnderTest.deleteAllExceptCurrentAsSuperuser(requestAccessToken);

        // Assert
        verify(this.usersSessionsRepository).deleteExceptAccessToken(requestAccessToken);
    }
}
