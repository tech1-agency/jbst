package io.tech1.framework.b2b.base.security.jwt.services.impl;

import io.tech1.framework.b2b.base.security.jwt.assistants.userdetails.JwtUserDetailsService;
import io.tech1.framework.b2b.base.security.jwt.cookies.CookieProvider;
import io.tech1.framework.b2b.base.security.jwt.domain.jwt.*;
import io.tech1.framework.b2b.base.security.jwt.domain.sessions.Session;
import io.tech1.framework.b2b.base.security.jwt.services.BaseUsersSessionsService;
import io.tech1.framework.b2b.base.security.jwt.services.TokensContextThrowerService;
import io.tech1.framework.b2b.base.security.jwt.services.TokensService;
import io.tech1.framework.b2b.base.security.jwt.sessions.SessionRegistry;
import io.tech1.framework.b2b.base.security.jwt.utils.SecurityJwtTokenUtils;
import io.tech1.framework.domain.exceptions.cookie.*;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.tech1.framework.b2b.base.security.jwt.tests.random.BaseSecurityJwtRandomUtility.randomValidDefaultClaims;
import static io.tech1.framework.domain.utilities.random.EntityUtility.entity;
import static io.tech1.framework.domain.utilities.random.RandomUtility.randomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class TokensServiceImplTest {

    @Configuration
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        @Bean
        JwtUserDetailsService jwtUserDetailsAssistant() {
            return mock(JwtUserDetailsService.class);
        }

        @Bean
        SessionRegistry sessionRegistry() {
            return mock(SessionRegistry.class);
        }

        @Bean
        TokensContextThrowerService tokenContextThrowerService() {
            return mock(TokensContextThrowerService.class);
        }

        @Bean
        BaseUsersSessionsService baseUsersSessionsService() {
            return mock(BaseUsersSessionsService.class);
        }

        @Bean
        CookieProvider cookieProvider() {
            return mock(CookieProvider.class);
        }

        @Bean
        SecurityJwtTokenUtils securityJwtTokenUtility() {
            return mock(SecurityJwtTokenUtils.class);
        }

        @Bean
        TokensService tokenService() {
            return new TokensServiceImpl(
                    this.jwtUserDetailsAssistant(),
                    this.sessionRegistry(),
                    this.tokenContextThrowerService(),
                    this.baseUsersSessionsService(),
                    this.cookieProvider(),
                    this.securityJwtTokenUtility()
            );
        }
    }

    // Assistants
    private final JwtUserDetailsService jwtUserDetailsService;
    // Session
    private final SessionRegistry sessionRegistry;
    // Services
    private final TokensContextThrowerService tokensContextThrowerService;
    private final BaseUsersSessionsService baseUsersSessionsService;
    // Cookie
    private final CookieProvider cookieProvider;
    // Utilities
    private final SecurityJwtTokenUtils securityJwtTokenUtils;

    private final TokensService componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.jwtUserDetailsService,
                this.sessionRegistry,
                this.tokensContextThrowerService,
                this.baseUsersSessionsService,
                this.cookieProvider,
                this.securityJwtTokenUtils
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.jwtUserDetailsService,
                this.sessionRegistry,
                this.tokensContextThrowerService,
                this.baseUsersSessionsService,
                this.cookieProvider,
                this.securityJwtTokenUtils
        );
    }

    @Test
    void getJwtUserByAccessTokenOrThrowTest() throws CookieAccessTokenInvalidException, CookieRefreshTokenInvalidException, CookieAccessTokenExpiredException {
        // Arrange
        var cookieAccessToken = entity(CookieAccessToken.class);
        var cookieRefreshToken = entity(CookieRefreshToken.class);
        var jwtAccessToken = cookieAccessToken.getJwtAccessToken();
        var jwtRefreshToken = cookieRefreshToken.getJwtRefreshToken();
        var accessTokenValidatedClaims = JwtTokenValidatedClaims.valid(jwtAccessToken, randomValidDefaultClaims());
        var refreshTokenValidatedClaims = JwtTokenValidatedClaims.valid(jwtRefreshToken, randomValidDefaultClaims());
        var jwtUser = entity(JwtUser.class);
        when(this.tokensContextThrowerService.verifyValidityOrThrow(jwtAccessToken)).thenReturn(accessTokenValidatedClaims);
        when(this.tokensContextThrowerService.verifyValidityOrThrow(jwtRefreshToken)).thenReturn(refreshTokenValidatedClaims);
        when(this.jwtUserDetailsService.loadUserByUsername(accessTokenValidatedClaims.safeGetUsername().identifier())).thenReturn(jwtUser);

        // Act
        var tuple2 = this.componentUnderTest.getJwtUserByAccessTokenOrThrow(cookieAccessToken, cookieRefreshToken);

        // Assert
        verify(this.tokensContextThrowerService).verifyValidityOrThrow(jwtAccessToken);
        verify(this.tokensContextThrowerService).verifyValidityOrThrow(jwtRefreshToken);
        verify(this.tokensContextThrowerService).verifyAccessTokenExpirationOrThrow(accessTokenValidatedClaims);
        verify(this.jwtUserDetailsService).loadUserByUsername(accessTokenValidatedClaims.safeGetUsername().identifier());
        assertThat(tuple2).isNotNull();
        assertThat(tuple2.a()).isEqualTo(jwtUser);
        assertThat(tuple2.b()).isEqualTo(jwtRefreshToken);
    }

    @Test
    void refreshSessionOrThrowTest() throws CookieRefreshTokenNotFoundException, CookieRefreshTokenInvalidException, CookieRefreshTokenExpiredException, CookieRefreshTokenDbNotFoundException {
        // Arrange
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var oldCookieRefreshToken = new CookieRefreshToken(randomString());
        var oldJwtRefreshToken = oldCookieRefreshToken.getJwtRefreshToken();
        var validatedClaims = JwtTokenValidatedClaims.valid(oldJwtRefreshToken, randomValidDefaultClaims());
        var user = entity(JwtUser.class);
        var jwtAccessToken = entity(JwtAccessToken.class);
        var newJwtRefreshToken = entity(JwtRefreshToken.class);
        var jwtRefreshToken = entity(JwtRefreshToken.class);
        when(this.cookieProvider.readJwtRefreshToken(request)).thenReturn(oldCookieRefreshToken);
        when(this.tokensContextThrowerService.verifyValidityOrThrow(oldJwtRefreshToken)).thenReturn(validatedClaims);
        when(this.tokensContextThrowerService.verifyDbPresenceOrThrow(validatedClaims, oldJwtRefreshToken)).thenReturn(user);
        when(this.securityJwtTokenUtils.createJwtAccessToken(user.getJwtTokenCreationParams())).thenReturn(jwtAccessToken);
        when(this.securityJwtTokenUtils.createJwtRefreshToken(user.getJwtTokenCreationParams())).thenReturn(newJwtRefreshToken);
        when(this.baseUsersSessionsService.refresh(user, oldCookieRefreshToken.getJwtRefreshToken(), newJwtRefreshToken, request)).thenReturn(jwtRefreshToken);

        // Act
        var responseUserSession1 = this.componentUnderTest.refreshSessionOrThrow(request, response);

        // Assert
        verify(this.cookieProvider).readJwtRefreshToken(request);
        verify(this.tokensContextThrowerService).verifyValidityOrThrow(oldJwtRefreshToken);
        verify(this.tokensContextThrowerService).verifyRefreshTokenExpirationOrThrow(validatedClaims);
        verify(this.tokensContextThrowerService).verifyDbPresenceOrThrow(validatedClaims, oldJwtRefreshToken);
        verify(this.securityJwtTokenUtils).createJwtAccessToken(user.getJwtTokenCreationParams());
        verify(this.securityJwtTokenUtils).createJwtRefreshToken(user.getJwtTokenCreationParams());
        verify(this.baseUsersSessionsService).refresh(user, oldJwtRefreshToken, newJwtRefreshToken, request);
        verify(this.cookieProvider).createJwtAccessCookie(jwtAccessToken, response);
        verify(this.cookieProvider).createJwtRefreshCookie(newJwtRefreshToken, response);
        var oldSessionAC = ArgumentCaptor.forClass(Session.class);
        var newSessionAC = ArgumentCaptor.forClass(Session.class);
        verify(this.sessionRegistry).renew(oldSessionAC.capture(), newSessionAC.capture());
        var oldSession = oldSessionAC.getValue();
        assertThat(oldSession).isNotNull();
        assertThat(oldSession.username()).isEqualTo(user.username());
        assertThat(oldSession.refreshToken().value()).isEqualTo(oldJwtRefreshToken.value());
        var newSession = newSessionAC.getValue();
        assertThat(newSession).isNotNull();
        assertThat(newSession.username()).isEqualTo(user.username());
        assertThat(newSession.refreshToken().value()).isEqualTo(newJwtRefreshToken.value());
        assertThat(responseUserSession1).isNotNull();
        assertThat(responseUserSession1.refreshToken()).isEqualTo(jwtRefreshToken);
    }
}