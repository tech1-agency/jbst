package jbst.iam.services.abstracts;

import jbst.iam.assistants.userdetails.JwtUserDetailsService;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.JwtTokenValidatedClaims;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.repositories.UsersSessionsRepository;
import jbst.iam.utils.SecurityJwtTokenUtils;
import tech1.framework.foundation.domain.exceptions.tokens.*;
import tech1.framework.foundation.domain.tuples.TuplePresence;
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

import static jbst.iam.domain.jwt.JwtTokenValidatedClaims.invalid;
import static jbst.iam.domain.jwt.JwtTokenValidatedClaims.valid;
import static jbst.iam.tests.random.BaseSecurityJwtRandomUtility.expiredClaims;
import static jbst.iam.tests.random.BaseSecurityJwtRandomUtility.validClaims;
import static tech1.framework.foundation.utilities.random.EntityUtility.entity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AbstractTokensContextThrowerServiceTest {

    private static Stream<Arguments> verifyAccessTokenExpirationOrThrow() {
        return Stream.of(
                Arguments.of(valid(JwtAccessToken.random(), validClaims()), false),
                Arguments.of(valid(JwtRefreshToken.random(), validClaims()), false),
                Arguments.of(valid(JwtAccessToken.random(), expiredClaims()), true),
                Arguments.of(valid(JwtRefreshToken.random(), expiredClaims()), false)
        );
    }

    private static Stream<Arguments> verifyRefreshTokenExpirationOrThrowTest() {
        return Stream.of(
                Arguments.of(valid(JwtAccessToken.random(), validClaims()), false),
                Arguments.of(valid(JwtRefreshToken.random(), validClaims()), false),
                Arguments.of(valid(JwtAccessToken.random(), expiredClaims()), false),
                Arguments.of(valid(JwtRefreshToken.random(), expiredClaims()), true)
        );
    }

    @Configuration
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        @Bean
        JwtUserDetailsService jwtUserDetailsService() {
            return mock(JwtUserDetailsService.class);
        }

        @Bean
        UsersSessionsRepository usersSessionsRepository() {
            return mock(UsersSessionsRepository.class);
        }

        @Bean
        SecurityJwtTokenUtils securityJwtTokenUtils() {
            return mock(SecurityJwtTokenUtils.class);
        }

        @Bean
        AbstractTokensContextThrowerService abstractTokensContextThrowerService() {
            return new AbstractTokensContextThrowerService(
                    this.jwtUserDetailsService(),
                    this.usersSessionsRepository(),
                    this.securityJwtTokenUtils()
            ) {};
        }
    }

    // Assistants
    private final JwtUserDetailsService jwtUserDetailsService;
    // Repositories
    private final UsersSessionsRepository usersSessionsRepository;
    // Utilities
    private final SecurityJwtTokenUtils securityJwtTokenUtils;

    private final AbstractTokensContextThrowerService componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.securityJwtTokenUtils
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.securityJwtTokenUtils
        );
    }

    @Test
    void verifyValidityAccessTokenTest() throws AccessTokenInvalidException {
        // Arrange
        var accessToken = JwtAccessToken.random();
        var validatedClaims = valid(accessToken, validClaims());
        when(this.securityJwtTokenUtils.validate(accessToken)).thenReturn(validatedClaims);

        // Act
        this.componentUnderTest.verifyValidityOrThrow(accessToken);

        // Assert
        verify(this.securityJwtTokenUtils).validate(accessToken);
    }

    @Test
    void verifyValidityAccessTokenThrowTest() {
        // Arrange
        var jwtAccessToken = JwtAccessToken.random();
        var validatedClaims = invalid(jwtAccessToken);
        when(this.securityJwtTokenUtils.validate(jwtAccessToken)).thenReturn(validatedClaims);

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.verifyValidityOrThrow(jwtAccessToken));

        // Assert
        verify(this.securityJwtTokenUtils).validate(jwtAccessToken);
        assertThat(throwable)
                .isInstanceOf(AccessTokenInvalidException.class)
                .hasMessageContaining("JWT access token is invalid");
    }

    @Test
    void verifyValidityRefreshTokenTest() throws RefreshTokenInvalidException {
        // Arrange
        var refreshToken = JwtRefreshToken.random();
        var validatedClaims = valid(refreshToken, validClaims());
        when(this.securityJwtTokenUtils.validate(refreshToken)).thenReturn(validatedClaims);

        // Act
        this.componentUnderTest.verifyValidityOrThrow(refreshToken);

        // Assert
        verify(this.securityJwtTokenUtils).validate(refreshToken);
    }

    @Test
    void verifyValidityRefreshTokenThrowTest() {
        // Arrange
        var refreshToken = JwtRefreshToken.random();
        var validatedClaims = invalid(refreshToken);
        when(this.securityJwtTokenUtils.validate(refreshToken)).thenReturn(validatedClaims);

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.verifyValidityOrThrow(refreshToken));

        // Assert
        verify(this.securityJwtTokenUtils).validate(refreshToken);
        assertThat(throwable)
                .isInstanceOf(RefreshTokenInvalidException.class)
                .hasMessageContaining("JWT refresh token is invalid");
    }

    @ParameterizedTest
    @MethodSource("verifyAccessTokenExpirationOrThrow")
    void verifyAccessTokenExpirationOrThrow(JwtTokenValidatedClaims validatedClaims, boolean throwableFlag) {
        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.verifyAccessTokenExpirationOrThrow(validatedClaims));

        // Assert
        if (throwableFlag) {
            assertThat(throwable)
                    .isInstanceOf(AccessTokenExpiredException.class)
                    .hasMessageContaining("JWT access token is expired. Username: " + validatedClaims.username());
        }
    }

    @ParameterizedTest
    @MethodSource("verifyRefreshTokenExpirationOrThrowTest")
    void verifyRefreshTokenExpirationOrThrowTest(JwtTokenValidatedClaims validatedClaims, boolean throwableFlag) {
        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.verifyRefreshTokenExpirationOrThrow(validatedClaims));

        // Assert
        if (throwableFlag) {
            assertThat(throwable)
                    .isInstanceOf(RefreshTokenExpiredException.class)
                    .hasMessageContaining("JWT refresh token is expired. Username: " + validatedClaims.username());
        }
    }

    @Test
    void verifyAccessTokenDbPresenceTest() throws AccessTokenDbNotFoundException {
        // Arrange
        var accessToken = JwtAccessToken.random();
        var validatedClaims = valid(accessToken, validClaims());
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(TuplePresence.present(entity(UserSession.class)));

        // Act
        this.componentUnderTest.verifyDbPresenceOrThrow(accessToken, validatedClaims);

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
    }

    @Test
    void verifyAccessTokenDbPresenceThrowTest() {
        // Arrange
        var accessToken = JwtAccessToken.random();
        var validatedClaims = valid(accessToken, validClaims());
        when(this.usersSessionsRepository.isPresent(accessToken)).thenReturn(TuplePresence.absent());

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.verifyDbPresenceOrThrow(accessToken, validatedClaims));

        // Assert
        verify(this.usersSessionsRepository).isPresent(accessToken);
        assertThat(throwable)
                .isInstanceOf(AccessTokenDbNotFoundException.class)
                .hasMessageContaining("JWT access token is not present in database. Username: " + validatedClaims.username());
    }

    @Test
    void verifyRefreshTokenDbPresenceTest() throws RefreshTokenDbNotFoundException {
        // Arrange
        var refreshToken = JwtRefreshToken.random();
        var validatedClaims = valid(refreshToken, validClaims());
        var user = entity(JwtUser.class);
        var session = entity(UserSession.class);
        when(this.jwtUserDetailsService.loadUserByUsername(validatedClaims.username().value())).thenReturn(user);
        when(this.usersSessionsRepository.isPresent(refreshToken)).thenReturn(TuplePresence.present(session));

        // Act
        var actual = this.componentUnderTest.verifyDbPresenceOrThrow(refreshToken, validatedClaims);

        // Assert
        verify(this.jwtUserDetailsService).loadUserByUsername(validatedClaims.username().value());
        verify(this.usersSessionsRepository).isPresent(refreshToken);
        assertThat(actual.a()).isEqualTo(user);
        assertThat(actual.b()).isEqualTo(session);
    }

    @Test
    void verifyRefreshTokenDbPresenceThrowTest() {
        // Arrange
        var refreshToken = JwtRefreshToken.random();
        var validatedClaims = valid(refreshToken, validClaims());
        when(this.usersSessionsRepository.isPresent(refreshToken)).thenReturn(TuplePresence.absent());

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.verifyDbPresenceOrThrow(refreshToken, validatedClaims));

        // Assert
        verify(this.usersSessionsRepository).isPresent(refreshToken);
        assertThat(throwable)
                .isInstanceOf(RefreshTokenDbNotFoundException.class)
                .hasMessageContaining("JWT refresh token is not present in database. Username: " + validatedClaims.username());
    }
}
