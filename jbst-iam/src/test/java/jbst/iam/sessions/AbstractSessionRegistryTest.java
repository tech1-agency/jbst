package jbst.iam.sessions;

import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.dto.responses.ResponseUserSession2;
import jbst.iam.domain.events.EventAuthenticationLogin;
import jbst.iam.domain.events.EventAuthenticationLogout;
import jbst.iam.domain.events.EventSessionExpired;
import jbst.iam.domain.events.EventSessionRefreshed;
import jbst.iam.domain.identifiers.UserSessionId;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.sessions.Session;
import jbst.iam.domain.sessions.SessionsExpiredTable;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.UsersSessionsRepository;
import jbst.iam.services.BaseUsersSessionsService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.geo.GeoLocation;
import jbst.foundation.domain.http.requests.UserAgentDetails;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.tuples.Tuple2;
import jbst.foundation.domain.tuples.Tuple3;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLogoutFull;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLogoutMin;
import jbst.foundation.incidents.domain.session.IncidentSessionExpired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static jbst.foundation.domain.http.requests.UserRequestMetadata.processed;
import static jbst.foundation.domain.tuples.TuplePresence.absent;
import static jbst.foundation.domain.tuples.TuplePresence.present;
import static jbst.foundation.utilities.random.EntityUtility.entity;
import static jbst.foundation.utilities.random.RandomUtility.randomString;
import static jbst.foundation.utilities.reflections.ReflectionUtility.setPrivateFieldOfSuperClass;
import static jbst.foundation.utilities.time.TimestampUtility.getCurrentTimestamp;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AbstractSessionRegistryTest {

    @Configuration
    static class ContextConfiguration {
        @Bean
        SecurityJwtEventsPublisher securityJwtPublisher() {
            return mock(SecurityJwtEventsPublisher.class);
        }

        @Bean
        SecurityJwtIncidentsPublisher securityJwtIncidentPublisher() {
            return mock(SecurityJwtIncidentsPublisher.class);
        }

        @Bean
        BaseUsersSessionsService userSessionService() {
            return mock(BaseUsersSessionsService.class);
        }

        @Bean
        UsersSessionsRepository usersSessionsRepository() {
            return mock(UsersSessionsRepository.class);
        }

        @Bean
        AbstractSessionRegistry sessionRegistry() {
            return new AbstractSessionRegistry(
                    this.securityJwtPublisher(),
                    this.securityJwtIncidentPublisher(),
                    this.userSessionService(),
                    this.usersSessionsRepository()
            ) {};
        }
    }

    // Publishers
    private final SecurityJwtEventsPublisher securityJwtPublisher;
    private final SecurityJwtIncidentsPublisher securityJwtIncidentPublisher;
    // Services
    private final BaseUsersSessionsService baseUsersSessionsService;
    // Repositories
    private final UsersSessionsRepository usersSessionsRepository;

    private final SessionRegistry componentUnderTest;

    @BeforeEach
    void beforeEach() throws Exception {
        // Clean sessions to execute a.k.a. integration test -> method "integrationFlow"
        setPrivateFieldOfSuperClass(this.componentUnderTest, "sessions", ConcurrentHashMap.newKeySet(), 1);
        reset(
                this.securityJwtIncidentPublisher,
                this.securityJwtPublisher,
                this.baseUsersSessionsService,
                this.usersSessionsRepository
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.securityJwtIncidentPublisher,
                this.securityJwtPublisher,
                this.baseUsersSessionsService,
                this.usersSessionsRepository
        );
    }

    private Session authenticateHardcoded(JwtAccessToken accessToken) throws NoSuchFieldException, IllegalAccessException {
        var session = new Session(Username.hardcoded(), accessToken, JwtRefreshToken.random());
        var sessions = ConcurrentHashMap.newKeySet();
        sessions.add(session);
        setPrivateFieldOfSuperClass(this.componentUnderTest, "sessions", sessions, 1);
        return session;
    }

    @Test
    void integrationTest() {
        // Arrange
        var session1 = new Session(Username.of("username1"), JwtAccessToken.random(), JwtRefreshToken.random());
        var session2 = new Session(Username.of("username2"), JwtAccessToken.random(), JwtRefreshToken.random());
        var session3 = new Session(Username.of("username3"), JwtAccessToken.random(), JwtRefreshToken.random());
        var session4 = new Session(Username.of("username4"), JwtAccessToken.random(), JwtRefreshToken.random());
        var rndSession = new Session(Username.random(), JwtAccessToken.random(), JwtRefreshToken.random());
        var dbUserSession1 = entity(UserSession.class);
        var dbUserSession2 = entity(UserSession.class);
        var dbUserSession3 = entity(UserSession.class);
        var dbUserSession4 = entity(UserSession.class);
        var rndDbUserSession = entity(UserSession.class);
        when(this.usersSessionsRepository.isPresent(session1.accessToken())).thenReturn(present(dbUserSession1));
        when(this.usersSessionsRepository.isPresent(session2.accessToken())).thenReturn(present(dbUserSession2));
        when(this.usersSessionsRepository.isPresent(session3.accessToken())).thenReturn(present(dbUserSession3));
        when(this.usersSessionsRepository.isPresent(session4.accessToken())).thenReturn(present(dbUserSession4));
        when(this.usersSessionsRepository.isPresent(rndSession.accessToken())).thenReturn(present(rndDbUserSession));

        // Iteration #1
        var activeSessionsUsernames1 = this.componentUnderTest.getActiveSessionsUsernamesIdentifiers();
        assertThat(activeSessionsUsernames1).isEmpty();
        assertThat(this.componentUnderTest.getActiveSessionsAccessTokens()).isEmpty();

        // Iteration #2
        this.componentUnderTest.register(session1);
        this.componentUnderTest.register(session2);
        var activeSessionsUsernames2 = this.componentUnderTest.getActiveSessionsUsernames();
        assertThat(activeSessionsUsernames2).hasSize(2);
        assertThat(this.componentUnderTest.getActiveSessionsAccessTokens()).hasSize(2);
        assertThat(activeSessionsUsernames2).isEqualTo(Set.of(session1.username(), session2.username()));

        // Iteration #3
        this.componentUnderTest.register(session3);
        this.componentUnderTest.logout(rndSession.username(), rndSession.accessToken());
        var activeSessionsUsernames3 = this.componentUnderTest.getActiveSessionsUsernames();
        assertThat(activeSessionsUsernames3).hasSize(3);
        assertThat(this.componentUnderTest.getActiveSessionsAccessTokens()).hasSize(3);
        assertThat(activeSessionsUsernames3).isEqualTo(Set.of(session1.username(), session2.username(), session3.username()));

        // Iteration #4
        this.componentUnderTest.register(session4);
        this.componentUnderTest.logout(session1.username(), session1.accessToken());
        this.componentUnderTest.logout(session2.username(), session2.accessToken());
        this.componentUnderTest.logout(session3.username(), session3.accessToken());
        var activeSessionsUsernames4 = this.componentUnderTest.getActiveSessionsUsernames();
        assertThat(activeSessionsUsernames4).hasSize(1);
        assertThat(this.componentUnderTest.getActiveSessionsAccessTokens()).hasSize(1);
        assertThat(activeSessionsUsernames4).isEqualTo(Set.of(session4.username()));

        // Iteration #5 (cleanup)
        this.componentUnderTest.logout(session4.username(), session4.accessToken());
        assertThat(this.componentUnderTest.getActiveSessionsUsernamesIdentifiers()).isEmpty();
        assertThat(this.componentUnderTest.getActiveSessionsUsernames()).isEmpty();
        assertThat(this.componentUnderTest.getActiveSessionsAccessTokens()).isEmpty();
        verify(this.usersSessionsRepository).isPresent(session1.accessToken());
        verify(this.usersSessionsRepository).isPresent(session2.accessToken());
        verify(this.usersSessionsRepository).isPresent(session3.accessToken());
        verify(this.usersSessionsRepository).isPresent(session4.accessToken());
        verify(this.securityJwtPublisher).publishAuthenticationLogin(new EventAuthenticationLogin(session1.username()));
        verify(this.securityJwtPublisher).publishAuthenticationLogin(new EventAuthenticationLogin(session2.username()));
        verify(this.securityJwtPublisher).publishAuthenticationLogin(new EventAuthenticationLogin(session3.username()));
        verify(this.securityJwtPublisher).publishAuthenticationLogin(new EventAuthenticationLogin(session4.username()));
        verify(this.securityJwtPublisher).publishAuthenticationLogout(new EventAuthenticationLogout(session1.username()));
        verify(this.securityJwtPublisher).publishAuthenticationLogout(new EventAuthenticationLogout(session2.username()));
        verify(this.securityJwtPublisher).publishAuthenticationLogout(new EventAuthenticationLogout(session3.username()));
        verify(this.securityJwtPublisher).publishAuthenticationLogout(new EventAuthenticationLogout(session4.username()));
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutFull(new IncidentAuthenticationLogoutFull(session1.username(), dbUserSession1.metadata()));
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutFull(new IncidentAuthenticationLogoutFull(session2.username(), dbUserSession2.metadata()));
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutFull(new IncidentAuthenticationLogoutFull(session3.username(), dbUserSession3.metadata()));
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutFull(new IncidentAuthenticationLogoutFull(session4.username(), dbUserSession4.metadata()));
        verify(this.usersSessionsRepository).delete(dbUserSession1.id());
        verify(this.usersSessionsRepository).delete(dbUserSession2.id());
        verify(this.usersSessionsRepository).delete(dbUserSession3.id());
        verify(this.usersSessionsRepository).delete(dbUserSession4.id());
    }

    @Test
    void registerTest() {
        // Act
        this.componentUnderTest.register(new Session(Username.hardcoded(), JwtAccessToken.random(), JwtRefreshToken.random()));
        this.componentUnderTest.register(new Session(Username.hardcoded(), JwtAccessToken.random(), JwtRefreshToken.random()));

        var duplicatedAccessToken = JwtAccessToken.random();
        var duplicatedRefreshToken = JwtRefreshToken.random();
        this.componentUnderTest.register(new Session(Username.hardcoded(), duplicatedAccessToken, duplicatedRefreshToken));
        this.componentUnderTest.register(new Session(Username.hardcoded(), duplicatedAccessToken, duplicatedRefreshToken));
        this.componentUnderTest.register(new Session(Username.hardcoded(), duplicatedAccessToken, duplicatedRefreshToken));

        // Assert
        assertThat(this.componentUnderTest.getActiveSessionsUsernamesIdentifiers()).hasSize(1);
        assertThat(this.componentUnderTest.getActiveSessionsUsernames()).hasSize(1);
        verify(this.securityJwtPublisher, times(3)).publishAuthenticationLogin(new EventAuthenticationLogin(Username.hardcoded()));
    }

    @Test
    void renewTest() {
        // Act
        this.componentUnderTest.renew(Username.hardcoded(), JwtRefreshToken.random(), JwtAccessToken.random(), JwtRefreshToken.random());
        this.componentUnderTest.renew(Username.hardcoded(), JwtRefreshToken.random(), JwtAccessToken.random(), JwtRefreshToken.random());

        var duplicatedAccessToken = JwtAccessToken.random();
        var duplicatedRefreshToken = JwtRefreshToken.random();
        this.componentUnderTest.renew(Username.hardcoded(), JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);
        this.componentUnderTest.renew(Username.hardcoded(), JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);
        this.componentUnderTest.renew(Username.hardcoded(), JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);
        this.componentUnderTest.renew(Username.hardcoded(), JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);

        // Assert
        assertThat(this.componentUnderTest.getActiveSessionsUsernames()).hasSize(1);
        assertThat(this.componentUnderTest.getActiveSessionsUsernamesIdentifiers()).isEqualTo(Set.of("jbst"));
        verify(this.securityJwtPublisher, times(3)).publishSessionRefreshed(any(EventSessionRefreshed.class));
    }

    @Test
    void logoutDbUserSessionPresentTest() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        var accessToken = JwtAccessToken.random();
        this.authenticateHardcoded(accessToken);
        var dbUserSession = entity(UserSession.class);
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(present(dbUserSession));

        // Act
        this.componentUnderTest.logout(Username.hardcoded(), accessToken);

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
        var eventAC = ArgumentCaptor.forClass(EventAuthenticationLogout.class);
        verify(this.securityJwtPublisher).publishAuthenticationLogout(eventAC.capture());
        verify(this.securityJwtPublisher).publishAuthenticationLogout(eventAC.capture());
        var incidentAC = ArgumentCaptor.forClass(IncidentAuthenticationLogoutFull.class);
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutFull(incidentAC.capture());
        var incident = incidentAC.getValue();
        assertThat(incident.username()).isEqualTo(Username.hardcoded());
        assertThat(incident.userRequestMetadata()).isEqualTo(dbUserSession.metadata());
        verify(this.usersSessionsRepository).delete(dbUserSession.id());
    }

    @Test
    void logoutDbUserSessionNotPresentTest() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        var accessToken = JwtAccessToken.random();
        var session = this.authenticateHardcoded(accessToken);
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(absent());

        // Act
        this.componentUnderTest.logout(Username.hardcoded(), accessToken);

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
        var eventAC = ArgumentCaptor.forClass(EventAuthenticationLogout.class);
        verify(this.securityJwtPublisher).publishAuthenticationLogout(eventAC.capture());
        assertThat(eventAC.getValue().username()).isEqualTo(session.username());
        var incidentAC = ArgumentCaptor.forClass(IncidentAuthenticationLogoutMin.class);
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutMin(incidentAC.capture());
        assertThat(incidentAC.getValue().username()).isEqualTo(Username.hardcoded());
    }

    @Test
    void cleanByExpiredRefreshTokensEnabledTest() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        var username1 = Username.of("username1");
        var username2 = Username.of("username2");
        var username3 = Username.of("username3");
        var session1 = new Session(username1, JwtAccessToken.random(), JwtRefreshToken.random());
        var session2 = new Session(username2, JwtAccessToken.random(), JwtRefreshToken.random());
        var session3 = new Session(username3, JwtAccessToken.random(), JwtRefreshToken.random());
        Set<Session> sessions = ConcurrentHashMap.newKeySet();
        sessions.add(session1);
        sessions.add(session2);
        sessions.add(session3);
        setPrivateFieldOfSuperClass(this.componentUnderTest, "sessions", sessions, 1);
        var dbUserSession1 = entity(UserSession.class);
        var dbUserSession2 = entity(UserSession.class);
        var dbUserSession3 = entity(UserSession.class);
        var sessionsExpiredTable = new SessionsExpiredTable(
                List.of(
                        new Tuple3<>(Username.hardcoded(), JwtRefreshToken.random(), UserRequestMetadata.random()),
                        new Tuple3<>(username3, session3.refreshToken(), dbUserSession3.metadata())
                ),
                Set.of(dbUserSession1.id(), dbUserSession2.id())
        );
        var usernames = Set.of(username1, username2, username3);
        when(this.baseUsersSessionsService.getExpiredRefreshTokensSessions(usernames)).thenReturn(sessionsExpiredTable);

        // Act
        this.componentUnderTest.cleanByExpiredRefreshTokens(usernames);

        // Assert
        verify(this.baseUsersSessionsService).getExpiredRefreshTokensSessions(usernames);
        assertThat(this.componentUnderTest.getActiveSessionsUsernames()).hasSize(2);
        assertThat(this.componentUnderTest.getActiveSessionsUsernamesIdentifiers()).isEqualTo(Set.of("username1", "username2"));
        var eseCaptor = ArgumentCaptor.forClass(EventSessionExpired.class);
        verify(this.securityJwtPublisher).publishSessionExpired(eseCaptor.capture());
        var eventSessionExpired = eseCaptor.getValue();
        assertThat(eventSessionExpired.session().username()).isEqualTo(username3);
        assertThat(eventSessionExpired.session().accessToken()).isEqualTo(session3.accessToken());
        assertThat(eventSessionExpired.session().refreshToken()).isEqualTo(session3.refreshToken());
        var seiCaptor = ArgumentCaptor.forClass(IncidentSessionExpired.class);
        verify(this.securityJwtIncidentPublisher).publishSessionExpired(seiCaptor.capture());
        var sessionExpiredIncident = seiCaptor.getValue();
        assertThat(sessionExpiredIncident.username()).isEqualTo(username3);
        assertThat(sessionExpiredIncident.userRequestMetadata()).isEqualTo(dbUserSession3.metadata());
        verify(this.usersSessionsRepository).delete(Set.of(dbUserSession1.id(), dbUserSession2.id()));
    }

    @Test
    void getSessionsTableTest() {
        // Arrange
        var username = entity(Username.class);
        var requestAccessToken = RequestAccessToken.random();

        Function<Tuple2<UserRequestMetadata, String>, ResponseUserSession2> sessionFnc =
                tuple2 -> ResponseUserSession2.of(entity(UserSessionId.class), getCurrentTimestamp(), Username.random(), requestAccessToken, new JwtAccessToken(tuple2.b()), tuple2.a());

        var validSession = sessionFnc.apply(new Tuple2<>(processed(GeoLocation.valid(), UserAgentDetails.valid()), requestAccessToken.value()));
        var invalidSession1 = sessionFnc.apply(new Tuple2<>(processed(GeoLocation.invalid(), UserAgentDetails.valid()), randomString()));
        var invalidSession2 = sessionFnc.apply(new Tuple2<>(processed(GeoLocation.valid(), UserAgentDetails.invalid()), randomString()));
        var invalidSession3 = sessionFnc.apply(new Tuple2<>(processed(GeoLocation.invalid(), UserAgentDetails.invalid()), randomString()));

        // userSessions, expectedSessionSize, expectedAnyProblems
        List<Tuple3<List<ResponseUserSession2>, Integer, Boolean>> cases = new ArrayList<>();
        cases.add(
                new Tuple3<>(
                        new ArrayList<>(List.of(validSession)),
                        1,
                        false
                )
        );
        cases.add(
                new Tuple3<>(
                        new ArrayList<>(List.of(validSession, invalidSession1)),
                        2,
                        true
                )
        );
        cases.add(
                new Tuple3<>(
                        new ArrayList<>(List.of(validSession, invalidSession1, invalidSession2)),
                        3,
                        true
                )
        );
        cases.add(
                new Tuple3<>(
                        new ArrayList<>(List.of(validSession, invalidSession1, invalidSession2, invalidSession3)),
                        4,
                        true
                )
        );

        // Act
        cases.forEach(item -> {
            // Arrange
            var userSessions = item.a();
            var expectedSessionSize = item.b();
            var expectedAnyProblems = item.c();
            when(this.usersSessionsRepository.getUsersSessionsTable(username, requestAccessToken)).thenReturn(userSessions);

            // Act
            var currentUserDbSessionsTable = this.componentUnderTest.getSessionsTable(username, requestAccessToken);

            // Assert
            verify(this.usersSessionsRepository).getUsersSessionsTable(username, requestAccessToken);
            assertThat(currentUserDbSessionsTable).isNotNull();
            assertThat(currentUserDbSessionsTable.sessions()).hasSize(expectedSessionSize);
            assertThat(currentUserDbSessionsTable.sessions().stream().filter(ResponseUserSession2::current).count()).isEqualTo(1);
            assertThat(currentUserDbSessionsTable.sessions().stream().filter(session -> "Current session".equals(session.activity())).count()).isEqualTo(1);
            assertThat(currentUserDbSessionsTable.anyProblem()).isEqualTo(expectedAnyProblems);

            reset(
                    this.usersSessionsRepository
            );
        });
    }
}
