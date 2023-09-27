package io.tech1.framework.b2b.base.security.jwt.sessions;

import io.tech1.framework.b2b.base.security.jwt.domain.db.UserSession;
import io.tech1.framework.b2b.base.security.jwt.domain.dto.responses.ResponseUserSession2;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventAuthenticationLogin;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventAuthenticationLogout;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventSessionExpired;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventSessionRefreshed;
import io.tech1.framework.b2b.base.security.jwt.domain.identifiers.UserSessionId;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.CookieAccessToken;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.JwtAccessToken;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.JwtRefreshToken;
import io.tech1.framework.b2b.base.security.jwt.domain.sessions.Session;
import io.tech1.framework.b2b.base.security.jwt.domain.sessions.SessionsExpiredTable;
import io.tech1.framework.b2b.base.security.jwt.events.publishers.SecurityJwtIncidentPublisher;
import io.tech1.framework.b2b.base.security.jwt.events.publishers.SecurityJwtPublisher;
import io.tech1.framework.b2b.base.security.jwt.repositories.UsersSessionsRepository;
import io.tech1.framework.b2b.base.security.jwt.services.BaseUsersSessionsService;
import io.tech1.framework.domain.base.Username;
import io.tech1.framework.domain.geo.GeoLocation;
import io.tech1.framework.domain.http.requests.UserAgentDetails;
import io.tech1.framework.domain.http.requests.UserRequestMetadata;
import io.tech1.framework.domain.tuples.Tuple2;
import io.tech1.framework.domain.tuples.Tuple3;
import io.tech1.framework.incidents.domain.authetication.IncidentAuthenticationLogoutFull;
import io.tech1.framework.incidents.domain.authetication.IncidentAuthenticationLogoutMin;
import io.tech1.framework.incidents.domain.session.IncidentSessionExpired;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static io.tech1.framework.domain.http.requests.UserRequestMetadata.processed;
import static io.tech1.framework.domain.tests.constants.TestsUsernamesConstants.TECH1;
import static io.tech1.framework.domain.tuples.TuplePresence.absent;
import static io.tech1.framework.domain.tuples.TuplePresence.present;
import static io.tech1.framework.domain.utilities.random.EntityUtility.entity;
import static io.tech1.framework.domain.utilities.random.RandomUtility.randomString;
import static io.tech1.framework.domain.utilities.reflections.ReflectionUtility.setPrivateFieldOfSuperClass;
import static io.tech1.framework.domain.utilities.time.TimestampUtility.getCurrentTimestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AbstractSessionRegistryTest {

    @Configuration
    static class ContextConfiguration {
        @Bean
        SecurityJwtPublisher securityJwtPublisher() {
            return mock(SecurityJwtPublisher.class);
        }

        @Bean
        SecurityJwtIncidentPublisher securityJwtIncidentPublisher() {
            return mock(SecurityJwtIncidentPublisher.class);
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
    private final SecurityJwtPublisher securityJwtPublisher;
    private final SecurityJwtIncidentPublisher securityJwtIncidentPublisher;
    // Services
    private final BaseUsersSessionsService baseUsersSessionsService;
    // Repositories
    private final UsersSessionsRepository usersSessionsRepository;

    private final SessionRegistry componentUnderTest;

    @BeforeEach
    void beforeEach() throws Exception {
        // WARNING: clean session to execute a.k.a. integration test -> method "integrationFlow"
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

    private Session authenticateTech1(JwtAccessToken accessToken) throws NoSuchFieldException, IllegalAccessException {
        var session = new Session(TECH1, accessToken, JwtRefreshToken.random());
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
        this.componentUnderTest.register(new Session(TECH1, JwtAccessToken.random(), JwtRefreshToken.random()));
        this.componentUnderTest.register(new Session(TECH1, JwtAccessToken.random(), JwtRefreshToken.random()));

        var duplicatedAccessToken = JwtAccessToken.random();
        var duplicatedRefreshToken = JwtRefreshToken.random();
        this.componentUnderTest.register(new Session(TECH1, duplicatedAccessToken, duplicatedRefreshToken));
        this.componentUnderTest.register(new Session(TECH1, duplicatedAccessToken, duplicatedRefreshToken));
        this.componentUnderTest.register(new Session(TECH1, duplicatedAccessToken, duplicatedRefreshToken));

        // Assert
        assertThat(this.componentUnderTest.getActiveSessionsUsernamesIdentifiers()).hasSize(1);
        assertThat(this.componentUnderTest.getActiveSessionsUsernames()).hasSize(1);
        verify(this.securityJwtPublisher, times(3)).publishAuthenticationLogin(new EventAuthenticationLogin(TECH1));
    }

    @Test
    void renewTest() {
        // Act
        this.componentUnderTest.renew(TECH1, JwtRefreshToken.random(), JwtAccessToken.random(), JwtRefreshToken.random());
        this.componentUnderTest.renew(TECH1, JwtRefreshToken.random(), JwtAccessToken.random(), JwtRefreshToken.random());

        var duplicatedAccessToken = JwtAccessToken.random();
        var duplicatedRefreshToken = JwtRefreshToken.random();
        this.componentUnderTest.renew(TECH1, JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);
        this.componentUnderTest.renew(TECH1, JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);
        this.componentUnderTest.renew(TECH1, JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);
        this.componentUnderTest.renew(TECH1, JwtRefreshToken.random(), duplicatedAccessToken, duplicatedRefreshToken);

        // Assert
        assertThat(this.componentUnderTest.getActiveSessionsUsernames()).hasSize(1);
        assertThat(this.componentUnderTest.getActiveSessionsUsernamesIdentifiers()).isEqualTo(Set.of("tech1"));
        verify(this.securityJwtPublisher, times(3)).publishSessionRefreshed(any(EventSessionRefreshed.class));
    }

    @Test
    void logoutDbUserSessionPresentTest() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        var accessToken = JwtAccessToken.random();
        this.authenticateTech1(accessToken);
        var dbUserSession = entity(UserSession.class);
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(present(dbUserSession));

        // Act
        this.componentUnderTest.logout(TECH1, accessToken);

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
        var eventAC = ArgumentCaptor.forClass(EventAuthenticationLogout.class);
        verify(this.securityJwtPublisher).publishAuthenticationLogout(eventAC.capture());
        verify(this.securityJwtPublisher).publishAuthenticationLogout(eventAC.capture());
        var incidentAC = ArgumentCaptor.forClass(IncidentAuthenticationLogoutFull.class);
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutFull(incidentAC.capture());
        var incident = incidentAC.getValue();
        assertThat(incident.username()).isEqualTo(TECH1);
        assertThat(incident.userRequestMetadata()).isEqualTo(dbUserSession.metadata());
        verify(this.usersSessionsRepository).delete(dbUserSession.id());
    }

    @Test
    void logoutDbUserSessionNotPresentTest() throws NoSuchFieldException, IllegalAccessException {
        // Arrange
        var accessToken = JwtAccessToken.random();
        var session = this.authenticateTech1(accessToken);
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(absent());

        // Act
        this.componentUnderTest.logout(TECH1, accessToken);

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
        var eventAC = ArgumentCaptor.forClass(EventAuthenticationLogout.class);
        verify(this.securityJwtPublisher).publishAuthenticationLogout(eventAC.capture());
        assertThat(eventAC.getValue().username()).isEqualTo(session.username());
        var incidentAC = ArgumentCaptor.forClass(IncidentAuthenticationLogoutMin.class);
        verify(this.securityJwtIncidentPublisher).publishAuthenticationLogoutMin(incidentAC.capture());
        assertThat(incidentAC.getValue().username()).isEqualTo(TECH1);
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
                        new Tuple3<>(TECH1, JwtRefreshToken.random(), UserRequestMetadata.random()),
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
        var cookie = CookieAccessToken.random();

        Function<Tuple2<UserRequestMetadata, String>, ResponseUserSession2> sessionFnc =
                tuple2 -> ResponseUserSession2.of(entity(UserSessionId.class), getCurrentTimestamp(), Username.random(), cookie, new JwtAccessToken(tuple2.b()), tuple2.a());

        var validSession = sessionFnc.apply(new Tuple2<>(processed(GeoLocation.valid(), UserAgentDetails.valid()), cookie.value()));
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
            when(this.usersSessionsRepository.getUsersSessionsTable(username, cookie)).thenReturn(userSessions);

            // Act
            var currentUserDbSessionsTable = this.componentUnderTest.getSessionsTable(username, cookie);

            // Assert
            verify(this.usersSessionsRepository).getUsersSessionsTable(username, cookie);
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
