package jbst.iam.domain.postgres.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jbst.iam.converters.columns.PostgresJwtAccessTokenConverter;
import jbst.iam.converters.columns.PostgresJwtRefreshTokenConverter;
import jbst.iam.converters.columns.PostgresUserRequestMetadataConverter;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.dto.responses.ResponseUserSession2;
import jbst.iam.domain.identifiers.UserSessionId;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.postgres.superclasses.PostgresDbAbstractPersistable1;
import lombok.*;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.converters.columns.PostgresUsernameConverter;
import jbst.foundation.domain.http.requests.UserRequestMetadata;

import java.util.List;

import static jbst.iam.domain.db.UserSession.ofNotPersisted;
import static jbst.iam.domain.db.UserSession.ofPersisted;

@SuppressWarnings("JpaDataSourceORMInspection")
// Lombok
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
// JPA
@Entity
@Table(name = PostgresDbUserSession.PG_TABLE_NAME)
public class PostgresDbUserSession extends PostgresDbAbstractPersistable1 {
    public static final String PG_TABLE_NAME = "tech1_users_sessions";

    @Convert(converter = PostgresUsernameConverter.class)
    @Column(nullable = false)
    private Username username;

    @Convert(converter = PostgresJwtAccessTokenConverter.class)
    @Column(name = "access_token", length = 4096, nullable = false)
    private JwtAccessToken accessToken;

    @Convert(converter = PostgresJwtRefreshTokenConverter.class)
    @Column(name = "refresh_token", length = 4096, nullable = false)
    private JwtRefreshToken refreshToken;

    @Convert(converter = PostgresUserRequestMetadataConverter.class)
    @Column(length = 65535, nullable = false)
    private UserRequestMetadata metadata;

    @Column(name = "metadata_renew_cron", nullable = false)
    private boolean metadataRenewCron;

    @Column(name = "metadata_renew_manually", nullable = false)
    private boolean metadataRenewManually;

    public PostgresDbUserSession(UserSession session) {
        if (session.persisted()) {
            this.id = session.id().value();
        }
        this.username = session.username();
        this.accessToken = session.accessToken();
        this.refreshToken = session.refreshToken();
        this.metadata = session.metadata();
        this.metadataRenewCron = false;
        this.metadataRenewManually = false;
    }

    public static PostgresDbUserSession random(String owner) {
        return new PostgresDbUserSession(UserSession.random(owner));
    }

    public static PostgresDbUserSession random(Username owner, String accessToken) {
        return new PostgresDbUserSession(UserSession.random(owner, accessToken));
    }

    public static PostgresDbUserSession random(String owner, String accessToken, String refreshToken) {
        return new PostgresDbUserSession(UserSession.random(owner, accessToken, refreshToken));
    }

    public static List<PostgresDbUserSession> dummies1() {
        var session1 = PostgresDbUserSession.random(Username.testsHardcoded().value(), "awt1", "rwt1");
        var session2 = PostgresDbUserSession.random(Username.testsHardcoded().value(), "awt2", "rwt2");
        var session3 = PostgresDbUserSession.random(Username.testsHardcoded().value(), "awt3", "rwt3");
        var session4 = PostgresDbUserSession.random(Username.testsHardcoded().value(), "awt4", "rwt4");
        var session5 = PostgresDbUserSession.random("user1", "atoken11", "rtoken11");
        var session6 = PostgresDbUserSession.random("user1", "atoken12", "rtoken12");
        var session7 = PostgresDbUserSession.random("sa", "atoken", "rtoken");
        return List.of(
                session1,
                session2,
                session3,
                session4,
                session5,
                session6,
                session7
        );
    }

    public static List<PostgresDbUserSession> dummies2() {
        var session1 = PostgresDbUserSession.random(Username.testsHardcoded(), "token1");
        var session2 = PostgresDbUserSession.random(Username.testsHardcoded(), "token2");
        var session3 = PostgresDbUserSession.random(Username.testsHardcoded(), "token3");
        var session4 = PostgresDbUserSession.random(Username.of("admin"), "token4");
        return List.of(session1, session2, session3, session4);
    }

    @JsonIgnore
    @Transient
    public UserSessionId userSessionId() {
        return new UserSessionId(this.id);
    }

    @JsonIgnore
    @Transient
    public UserSession userSession() {
        if (this.isNew()) {
            return ofNotPersisted(this.username, this.accessToken, this.refreshToken, this.metadata);
        } else {
            return ofPersisted(
                    this.userSessionId(),
                    this.createdAt,
                    this.updatedAt,
                    this.username,
                    this.accessToken,
                    this.refreshToken,
                    this.metadata,
                    this.metadataRenewCron,
                    this.metadataRenewManually
            );
        }
    }

    @JsonIgnore
    @Transient
    public ResponseUserSession2 responseUserSession2(RequestAccessToken requestAccessToken) {
        return ResponseUserSession2.of(
                this.userSessionId(),
                this.updatedAt,
                this.username,
                requestAccessToken,
                this.accessToken,
                this.metadata
        );
    }
}
