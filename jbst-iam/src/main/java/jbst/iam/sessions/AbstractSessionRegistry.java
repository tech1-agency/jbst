package jbst.iam.sessions;

import jbst.foundation.domain.base.Username;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLogoutFull;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLogoutMin;
import jbst.foundation.incidents.domain.session.IncidentSessionExpired;
import jbst.iam.domain.dto.responses.ResponseUserSessionsTable;
import jbst.iam.domain.events.EventAuthenticationLogin;
import jbst.iam.domain.events.EventAuthenticationLogout;
import jbst.iam.domain.events.EventSessionExpired;
import jbst.iam.domain.events.EventSessionRefreshed;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.sessions.Session;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.UsersSessionsRepository;
import jbst.iam.services.BaseUsersSessionsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static jbst.foundation.domain.constants.JbstConstants.Logs.USER_ACTION;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractSessionRegistry implements SessionRegistry {
    protected final ConcurrentHashMap.KeySetView<Session, Boolean> sessions = ConcurrentHashMap.newKeySet();

    // Publishers
    protected final SecurityJwtEventsPublisher securityJwtEventsPublisher;
    protected final SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher;
    // Services
    protected final BaseUsersSessionsService baseUsersSessionsService;
    // Repositories
    protected final UsersSessionsRepository usersSessionsRepository;

    @Override
    public Set<String> getActiveSessionsUsernamesIdentifiers() {
        return this.sessions.stream()
                .map(session -> session.username().value())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Username> getActiveSessionsUsernames() {
        return this.sessions.stream()
                .map(Session::username)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<JwtAccessToken> getActiveSessionsAccessTokens() {
        return this.sessions.stream()
                .map(Session::accessToken)
                .collect(Collectors.toSet());
    }

    @Override
    public void register(Session session) {
        var username = session.username();
        boolean added = this.sessions.add(session);
        if (added) {
            LOGGER.debug(USER_ACTION, username, "Session Registration");
            this.securityJwtEventsPublisher.publishAuthenticationLogin(new EventAuthenticationLogin(username));
        }
    }

    @Override
    public void renew(Username username, JwtRefreshToken oldRefreshToken, JwtAccessToken newAccessToken, JwtRefreshToken newRefreshToken) {
        this.sessions.removeIf(session -> session.refreshToken().equals(oldRefreshToken));
        var newSession = new Session(username, newAccessToken, newRefreshToken);
        var added = this.sessions.add(newSession);
        if (added) {
            LOGGER.debug(USER_ACTION, username, "Session Renew");
            this.securityJwtEventsPublisher.publishSessionRefreshed(new EventSessionRefreshed(newSession));
        }
    }

    @Override
    public void logout(Username username, JwtAccessToken accessToken) {
        LOGGER.debug(USER_ACTION, username, "Session Deletion");
        var removed = this.sessions.removeIf(session -> session.accessToken().equals(accessToken));
        if (removed) {
            this.securityJwtEventsPublisher.publishAuthenticationLogout(new EventAuthenticationLogout(username));

            var sessionTP = this.usersSessionsRepository.isPresent(accessToken);

            if (sessionTP.present()) {
                var session = sessionTP.value();
                this.securityJwtIncidentsPublisher.publishAuthenticationLogoutFull(new IncidentAuthenticationLogoutFull(username, session.metadata()));
                this.usersSessionsRepository.delete(session.id());
            } else {
                this.securityJwtIncidentsPublisher.publishAuthenticationLogoutMin(new IncidentAuthenticationLogoutMin(username));
            }
        }

    }

    @Override
    public void cleanByExpiredRefreshTokens(Set<Username> usernames) {
        var sessionsValidatedTuple2 = this.baseUsersSessionsService.getExpiredRefreshTokensSessions(usernames);

        sessionsValidatedTuple2.expiredSessions().forEach(tuple -> {
            var username = tuple.a();
            var refreshToken = tuple.b();
            var metadata = tuple.c();

            LOGGER.debug(USER_ACTION, username, "Session Expiration");
            var sessionOpt = this.sessions.stream()
                    .filter(session -> session.refreshToken().equals(refreshToken))
                    .findFirst();

            if (sessionOpt.isPresent()) {
                var session = sessionOpt.get();
                this.sessions.remove(session);
                this.securityJwtEventsPublisher.publishSessionExpired(new EventSessionExpired(session));
                this.securityJwtIncidentsPublisher.publishSessionExpired(new IncidentSessionExpired(username, metadata));
            }
        });

        var deleted = this.usersSessionsRepository.delete(sessionsValidatedTuple2.expiredOrInvalidSessionIds());
        LOGGER.debug("JWT expired or invalid refresh tokens ids was successfully deleted. Count: {}", deleted);
    }

    @Override
    public ResponseUserSessionsTable getSessionsTable(Username username, RequestAccessToken requestAccessToken) {
        return ResponseUserSessionsTable.of(this.usersSessionsRepository.getUsersSessionsTable(username, requestAccessToken));
    }
}
