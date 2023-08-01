package io.tech1.framework.b2b.base.security.jwt.domain.jwt;

import io.tech1.framework.domain.base.Username;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.tech1.framework.b2b.base.security.jwt.tests.random.BaseSecurityJwtRandomUtility.validDefaultClaims;
import static io.tech1.framework.domain.tests.constants.TestsUsernamesConstants.TECH1;
import static io.tech1.framework.domain.utilities.random.EntityUtility.entity;
import static io.tech1.framework.domain.utilities.time.TimestampUtility.getCurrentTimestamp;
import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenValidatedClaimsTest {
    private static final Username INVALID = Username.of("invalid");

    @Test
    void invalidAccessTokenTest() {
        // Arrange
        var token = entity(JwtAccessToken.class);

        // Act
        var validatedClaims = JwtTokenValidatedClaims.invalid(token);

        // Assert
        assertThat(validatedClaims.valid()).isFalse();
        assertThat(validatedClaims.isInvalid()).isTrue();
        assertThat(validatedClaims.isExpired()).isTrue();
        assertThat(validatedClaims.isAccess()).isTrue();
        assertThat(validatedClaims.isRefresh()).isFalse();
        assertThat(validatedClaims.jwtToken()).isEqualTo(token.value());
        assertThat(validatedClaims.username()).isEqualTo(INVALID);
        assertThat(validatedClaims.issuedAt().getTime()).isZero();
        assertThat(validatedClaims.getExpirationTimestamp()).isZero();
        assertThat(validatedClaims.authorities()).isEmpty();
    }

    @Test
    void invalidRefreshTokenTest() {
        // Arrange
        var token = entity(JwtRefreshToken.class);

        // Act
        var validatedClaims = JwtTokenValidatedClaims.invalid(token);

        // Assert
        assertThat(validatedClaims.valid()).isFalse();
        assertThat(validatedClaims.isInvalid()).isTrue();
        assertThat(validatedClaims.isExpired()).isTrue();
        assertThat(validatedClaims.isAccess()).isFalse();
        assertThat(validatedClaims.isRefresh()).isTrue();
        assertThat(validatedClaims.jwtToken()).isEqualTo(token.value());
        assertThat(validatedClaims.username()).isEqualTo(INVALID);
        assertThat(validatedClaims.issuedAt().getTime()).isZero();
        assertThat(validatedClaims.getExpirationTimestamp()).isZero();
        assertThat(validatedClaims.authorities()).isEmpty();
    }

    @RepeatedTest(10)
    void validAccessTokenTest() {
        // Arrange
        var token = entity(JwtAccessToken.class);

        // Act
        var validatedClaims = JwtTokenValidatedClaims.valid(token, validDefaultClaims());

        // Assert
        assertThat(validatedClaims.valid()).isTrue();
        assertThat(validatedClaims.isInvalid()).isFalse();
        assertThat(validatedClaims.isExpired()).isFalse();
        assertThat(validatedClaims.isAccess()).isTrue();
        assertThat(validatedClaims.isRefresh()).isFalse();
        assertThat(validatedClaims.jwtToken()).isEqualTo(token.value());
        assertThat(validatedClaims.username()).isEqualTo(TECH1);
        assertThat(validatedClaims.issuedAt().getTime()).isLessThanOrEqualTo(getCurrentTimestamp());
        // 3600000L == 1 hour
        assertThat(validatedClaims.getExpirationTimestamp() - validatedClaims.issuedAt().getTime()).isEqualTo(3600000L);
        assertThat(validatedClaims.authoritiesAsStrings()).isEqualTo(Set.of("admin", "user"));
    }

    @RepeatedTest(10)
    void validRefreshTokenTest() {
        // Arrange
        var token = entity(JwtRefreshToken.class);

        // Act
        var validatedClaims = JwtTokenValidatedClaims.valid(token, validDefaultClaims());

        // Assert
        assertThat(validatedClaims.valid()).isTrue();
        assertThat(validatedClaims.isInvalid()).isFalse();
        assertThat(validatedClaims.isExpired()).isFalse();
        assertThat(validatedClaims.isAccess()).isFalse();
        assertThat(validatedClaims.isRefresh()).isTrue();
        assertThat(validatedClaims.jwtToken()).isEqualTo(token.value());
        assertThat(validatedClaims.username()).isEqualTo(TECH1);
        assertThat(validatedClaims.issuedAt().getTime()).isLessThanOrEqualTo(getCurrentTimestamp());
        // 3600000L == 1 hour
        assertThat(validatedClaims.getExpirationTimestamp() - validatedClaims.issuedAt().getTime()).isEqualTo(3600000L);
        assertThat(validatedClaims.authoritiesAsStrings()).isEqualTo(Set.of("admin", "user"));
    }
}
