package io.tech1.framework.b2b.base.security.jwt.sessions;

import io.tech1.framework.b2b.base.security.jwt.domain.dto.responses.ResponseUserSessionsTable;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventAuthenticationLogin;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventAuthenticationLogout;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventSessionExpired;
import io.tech1.framework.b2b.base.security.jwt.domain.events.EventSessionRefreshed;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.CookieRefreshToken;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.JwtRefreshToken;
import io.tech1.framework.b2b.base.security.jwt.domain.sessions.Session;
import io.tech1.framework.b2b.base.security.jwt.events.publishers.SecurityJwtIncidentPublisher;
import io.tech1.framework.b2b.base.security.jwt.events.publishers.SecurityJwtPublisher;
import io.tech1.framework.b2b.base.security.jwt.repositories.AnyDbUsersSessionsRepository;
import io.tech1.framework.b2b.base.security.jwt.services.BaseUsersSessionsService;
import io.tech1.framework.domain.base.Username;
import io.tech1.framework.incidents.domain.authetication.IncidentAuthenticationLogoutFull;
import io.tech1.framework.incidents.domain.authetication.IncidentAuthenticationLogoutMin;
import io.tech1.framework.incidents.domain.session.IncidentSessionExpired;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.tech1.framework.domain.constants.FrameworkLogsConstants.*;
import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractSessionRegistry implements SessionRegistry {

    protected final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    // Publishers
    protected final SecurityJwtPublisher securityJwtPublisher;
    protected final SecurityJwtIncidentPublisher securityJwtIncidentPublisher;
    // Services
    protected final BaseUsersSessionsService baseUsersSessionsService;
    // Repositories
    protected final AnyDbUsersSessionsRepository anyDbUsersSessionsRepository;

    @Override
    public Set<String> getActiveSessionsUsernamesIdentifiers() {
        return this.sessions.stream()
                .map(session -> session.username().identifier())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Username> getActiveSessionsUsernames() {
        return this.sessions.stream()
                .map(Session::username)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<JwtRefreshToken> getActiveSessionsRefreshTokens() {
        return this.sessions.stream()
                .map(Session::refreshToken)
                .collect(Collectors.toSet());
    }

    @Override
    public void register(Session session) {
        var username = session.username();
        boolean added = this.sessions.add(session);
        if (added) {
            LOGGER.debug(SESSION_REGISTRY_REGISTER_SESSION, username);
            this.securityJwtPublisher.publishAuthenticationLogin(new EventAuthenticationLogin(username));
        }
    }

    @Override
    public void renew(Session oldSession, Session newSession) {
        this.sessions.remove(oldSession);
        boolean added = this.sessions.add(newSession);
        if (added) {
            var username = newSession.username();
            LOGGER.debug(SESSION_REGISTRY_RENEW_SESSION, username);

            this.securityJwtPublisher.publishSessionRefreshed(new EventSessionRefreshed(newSession));
        }
    }

    @Override
    public void logout(Session session) {
        var username = session.username();
        LOGGER.debug(SESSION_REGISTRY_REMOVE_SESSION, username);
        this.sessions.remove(session);

        this.securityJwtPublisher.publishAuthenticationLogout(new EventAuthenticationLogout(session));

        var jwtRefreshToken = session.refreshToken();
        var dbUserSession = this.anyDbUsersSessionsRepository.findByRefreshTokenAsAny(jwtRefreshToken);

        if (nonNull(dbUserSession)) {
            this.securityJwtIncidentPublisher.publishAuthenticationLogoutFull(new IncidentAuthenticationLogoutFull(username, dbUserSession.metadata()));
            this.anyDbUsersSessionsRepository.deleteByRefreshToken(jwtRefreshToken);
        } else {
            this.securityJwtIncidentPublisher.publishAuthenticationLogoutMin(new IncidentAuthenticationLogoutMin(username));
        }
    }

    @Override
    public void cleanByExpiredRefreshTokens(Set<Username> usernames) {
        var sessionsValidatedTuple2 = this.baseUsersSessionsService.getExpiredSessions(usernames);

        sessionsValidatedTuple2.expiredSessions().forEach(tuple2 -> {
            var username = tuple2.a();
            var requestMetadata = tuple2.b();
            var jwtRefreshToken = tuple2.c();
            var session = new Session(username, jwtRefreshToken);
            LOGGER.debug(SESSION_REGISTRY_EXPIRE_SESSION, username);
            this.sessions.remove(session);
            this.securityJwtPublisher.publishSessionExpired(new EventSessionExpired(session));
            this.securityJwtIncidentPublisher.publishSessionExpired(new IncidentSessionExpired(username, requestMetadata));
        });

        var deleted = this.anyDbUsersSessionsRepository.deleteByUsersSessionsIds(sessionsValidatedTuple2.expiredOrInvalidSessionIds());
        LOGGER.debug("JWT expired or invalid refresh tokens ids was successfully deleted. Count: `{}`", deleted);
    }

    @Override
    public ResponseUserSessionsTable getSessionsTable(Username username, CookieRefreshToken cookie) {
        return ResponseUserSessionsTable.of(this.anyDbUsersSessionsRepository.findByUsernameAndCookieAsSession2(username, cookie));
    }
}