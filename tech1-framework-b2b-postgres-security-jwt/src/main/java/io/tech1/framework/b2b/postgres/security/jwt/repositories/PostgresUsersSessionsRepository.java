package io.tech1.framework.b2b.postgres.security.jwt.repositories;

import io.tech1.framework.b2b.base.security.jwt.domain.db.AnyDbUserSession;
import io.tech1.framework.b2b.base.security.jwt.domain.dto.responses.ResponseUserSession2;
import io.tech1.framework.b2b.base.security.jwt.domain.identifiers.UserSessionId;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.CookieRefreshToken;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.JwtRefreshToken;
import io.tech1.framework.b2b.base.security.jwt.repositories.AnyDbUsersSessionsRepository;
import io.tech1.framework.b2b.postgres.security.jwt.domain.db.PostgresDbUserSession;
import io.tech1.framework.domain.base.Username;
import io.tech1.framework.domain.tuples.Tuple2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.tech1.framework.domain.asserts.Asserts.assertNonNullOrThrow;
import static io.tech1.framework.domain.utilities.exceptions.ExceptionsMessagesUtility.entityNotFound;
import static java.util.Objects.nonNull;

@SuppressWarnings("JpaQlInspection")
public interface PostgresUsersSessionsRepository extends JpaRepository<PostgresDbUserSession, String>, AnyDbUsersSessionsRepository {
    // ================================================================================================================
    // Any
    // ================================================================================================================
    default boolean isPresent(JwtRefreshToken jwtRefreshToken) {
        return nonNull(this.findByRefreshTokenAsAny(jwtRefreshToken));
    }

    default AnyDbUserSession getById(UserSessionId sessionId) {
        return this.findById(sessionId.value()).map(PostgresDbUserSession::anyDbUserSession).orElse(null);
    }

    default AnyDbUserSession requirePresence(UserSessionId sessionId) {
        var session = this.getById(sessionId);
        assertNonNullOrThrow(session, entityNotFound("Session", sessionId.value()));
        return session;
    }

    default List<ResponseUserSession2> findByUsernameAndCookieAsSession2(Username username, CookieRefreshToken cookie) {
        return this.findByUsername(username).stream()
                .map(session -> session.responseUserSession2(cookie))
                .collect(Collectors.toList());
    }

    default List<Tuple2<ResponseUserSession2, JwtRefreshToken>> findAllByCookieAsSession2(CookieRefreshToken cookie) {
        return this.findAll().stream()
                .map(session ->
                        new Tuple2<>(
                                ResponseUserSession2.of(
                                        session.getUsername(),
                                        session.getMetadata(),
                                        session.getJwtRefreshToken(),
                                        cookie
                                ),
                                session.getJwtRefreshToken()
                        )
                )
                .collect(Collectors.toList());
    }

    default List<AnyDbUserSession> findByUsernameInAsAny(Set<Username> usernames) {
        return this.findByUsernameIn(usernames).stream()
                .map(PostgresDbUserSession::anyDbUserSession)
                .collect(Collectors.toList());
    }

    default AnyDbUserSession findByRefreshTokenAsAny(JwtRefreshToken jwtRefreshToken) {
        return this.findById(jwtRefreshToken.value()).map(PostgresDbUserSession::anyDbUserSession).orElse(null);
    }

    default UserSessionId saveAs(AnyDbUserSession userSession) {
        var entity = this.save(new PostgresDbUserSession(userSession));
        return entity.userSessionId();
    }

    default void delete(UserSessionId sessionId) {
        this.deleteById(sessionId.value());
    }

    default long deleteByUsersSessionsIds(List<UserSessionId> sessionsIds) {
        return this.deleteByIdIn(sessionsIds.stream().map(UserSessionId::value).toList());
    }

    default void deleteByRefreshToken(JwtRefreshToken jwtRefreshToken) {
        this.deleteById(jwtRefreshToken.value());
    }

    @Transactional
    default void deleteByUsernameExceptSessionIdEqualsRefreshToken(Username username, CookieRefreshToken cookie) {
        this.deleteByUsernameExceptSessionId(username, cookie.value());
    }

    @Transactional
    default void deleteExceptSessionIdEqualsRefreshToken(CookieRefreshToken cookie) {
        this.deleteExceptSessionId(cookie.value());
    }

    // ================================================================================================================
    // Spring Data
    // ================================================================================================================
    List<PostgresDbUserSession> findByUsername(Username username);
    List<PostgresDbUserSession> findByUsernameIn(Set<Username> usernames);

    long deleteByIdIn(List<String> ids);

    // ================================================================================================================
    // Queries
    // ================================================================================================================
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM PostgresDbUserSession s WHERE s.username IN :usernames")
    void deleteByUsernames(@Param("usernames") Set<Username> usernames);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM PostgresDbUserSession s WHERE s.username = :username AND s.id != :sessionId")
    void deleteByUsernameExceptSessionId(@Param("username") Username username, @Param("sessionId") String sessionId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM PostgresDbUserSession s WHERE s.id != :sessionId")
    void deleteExceptSessionId(@Param("sessionId") String sessionId);
}