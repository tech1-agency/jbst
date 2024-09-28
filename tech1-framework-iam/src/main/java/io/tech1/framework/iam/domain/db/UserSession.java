package io.tech1.framework.iam.domain.db;

import io.tech1.framework.iam.domain.identifiers.UserSessionId;
import io.tech1.framework.iam.domain.jwt.JwtAccessToken;
import io.tech1.framework.iam.domain.jwt.JwtRefreshToken;
import tech1.framework.foundation.domain.base.Username;
import tech1.framework.foundation.domain.http.requests.UserRequestMetadata;

import static tech1.framework.foundation.utilities.random.EntityUtility.entity;
import static tech1.framework.foundation.utilities.random.RandomUtility.randomBoolean;
import static tech1.framework.foundation.utilities.time.TimestampUtility.getCurrentTimestamp;

public record UserSession(
        boolean persisted,
        UserSessionId id,
        long createdAt,
        long updatedAt,
        Username username,
        JwtAccessToken accessToken,
        JwtRefreshToken refreshToken,
        UserRequestMetadata metadata,
        boolean metadataRenewCron,
        boolean metadataRenewManually
) {

    public static UserSession randomPersistedSession() {
        return UserSession.ofPersisted(
                UserSessionId.random(),
                getCurrentTimestamp(),
                getCurrentTimestamp(),
                Username.random(),
                JwtAccessToken.random(),
                JwtRefreshToken.random(),
                UserRequestMetadata.random(),
                randomBoolean(),
                randomBoolean()
        );
    }

    public static UserSession ofPersisted(
            UserSessionId id,
            long createdAt,
            long updatedAt,
            Username username,
            JwtAccessToken accessToken,
            JwtRefreshToken refreshToken,
            UserRequestMetadata metadata,
            boolean metadataRenewCron,
            boolean metadataRenewManually
    ) {
        return new UserSession(
                true,
                id,
                createdAt,
                updatedAt,
                username,
                accessToken,
                refreshToken,
                metadata,
                metadataRenewCron,
                metadataRenewManually
        );
    }

    public static UserSession ofNotPersisted(
            Username username,
            JwtAccessToken accessToken,
            JwtRefreshToken refreshToken,
            UserRequestMetadata metadata
    ) {
        var currentTimestamp = getCurrentTimestamp();
        return new UserSession(
                false,
                UserSessionId.undefined(),
                currentTimestamp,
                currentTimestamp,
                username,
                accessToken,
                refreshToken,
                metadata,
                false,
                false
        );
    }

    public static UserSession random(Username owner, JwtAccessToken accessToken, JwtRefreshToken refreshToken) {
        return UserSession.ofPersisted(
                UserSessionId.random(),
                getCurrentTimestamp(),
                getCurrentTimestamp(),
                owner,
                accessToken,
                refreshToken,
                UserRequestMetadata.random(),
                false,
                false
        );
    }

    public static UserSession random(String owner, String accessToken, String refreshToken) {
        return random(
                Username.of(owner),
                JwtAccessToken.of(accessToken),
                JwtRefreshToken.of(refreshToken)
        );
    }

    public static UserSession random(Username owner, String accessToken) {
        return random(owner.value(), accessToken, entity(JwtRefreshToken.class).value());
    }

    public static UserSession random(String owner) {
        return random(Username.of(owner), entity(JwtAccessToken.class).value());
    }

    public boolean isRenewRequired() {
        return this.metadataRenewCron || this.metadataRenewManually;
    }
}
