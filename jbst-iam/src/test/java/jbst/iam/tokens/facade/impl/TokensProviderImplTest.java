package jbst.iam.tokens.facade.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.tokens.facade.TokensProvider;
import jbst.iam.tokens.providers.TokenCookiesProvider;
import jbst.iam.tokens.providers.TokenHeadersProvider;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import tech1.framework.foundation.domain.exceptions.tokens.AccessTokenNotFoundException;
import tech1.framework.foundation.domain.exceptions.tokens.CsrfTokenNotFoundException;
import tech1.framework.foundation.domain.exceptions.tokens.RefreshTokenNotFoundException;
import tech1.framework.foundation.domain.properties.ApplicationFrameworkProperties;
import tech1.framework.foundation.domain.properties.base.JwtToken;
import tech1.framework.foundation.domain.properties.base.JwtTokenStorageMethod;
import tech1.framework.foundation.domain.properties.base.TimeAmount;
import tech1.framework.foundation.domain.properties.configs.SecurityJwtConfigs;
import tech1.framework.foundation.domain.properties.configs.security.jwt.*;

import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.mockito.Mockito.*;
import static tech1.framework.foundation.utilities.random.RandomUtility.randomString;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
class TokensProviderImplTest {

    private static Stream<Arguments> jwtTokenStoragesArgs() {
        return Stream.of(
                Arguments.of(JwtTokenStorageMethod.COOKIES),
                Arguments.of(JwtTokenStorageMethod.HEADERS)
        );
    }

    @Configuration
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        @Bean
        ApplicationFrameworkProperties applicationFrameworkProperties() {
            return mock(ApplicationFrameworkProperties.class);
        }

        @Bean("tokensCookiesProvider")
        TokenCookiesProvider tokensCookiesProvider() {
            return mock(TokenCookiesProvider.class);
        }

        @Bean("tokensHeadersProvider")
        TokenHeadersProvider tokensHeadersProvider() {
            return mock(TokenHeadersProvider.class);
        }

        @Bean
        TokensProvider tokensProvider() {
            return new TokensProviderImpl(
                    this.tokensCookiesProvider(),
                    this.tokensHeadersProvider(),
                    this.applicationFrameworkProperties()
            );
        }
    }

    private final TokenCookiesProvider tokensCookiesProvider;
    private final TokenHeadersProvider tokensHeadersProvider;
    private final ApplicationFrameworkProperties applicationFrameworkProperties;

    private final TokensProvider componentUnderTest;

    @Autowired
    public TokensProviderImplTest(
            @Qualifier("tokensCookiesProvider") TokenCookiesProvider tokensCookiesProvider,
            @Qualifier("tokensHeadersProvider") TokenHeadersProvider tokensHeadersProvider,
            ApplicationFrameworkProperties applicationFrameworkProperties,
            TokensProvider tokensProvider
    ) {
        this.tokensCookiesProvider = tokensCookiesProvider;
        this.tokensHeadersProvider = tokensHeadersProvider;
        this.applicationFrameworkProperties = applicationFrameworkProperties;
        this.componentUnderTest = tokensProvider;
    }

    @BeforeEach
    void beforeEach() {
        reset(
                this.tokensCookiesProvider,
                this.tokensHeadersProvider,
                this.applicationFrameworkProperties
        );
    }

    @AfterEach
    void afterEach() {
        verify(this.applicationFrameworkProperties).getSecurityJwtConfigs();
        verifyNoMoreInteractions(
                this.tokensCookiesProvider,
                this.tokensHeadersProvider,
                this.applicationFrameworkProperties
        );
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void createResponseAccessToken(JwtTokenStorageMethod method) {
        // Arrange
        this.mockProperties(method);
        var jwtAccessToken = new JwtAccessToken(randomString());
        var response = mock(HttpServletResponse.class);

        // Act
        this.componentUnderTest.createResponseAccessToken(jwtAccessToken, response);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).createResponseAccessToken(jwtAccessToken, response);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).createResponseAccessToken(jwtAccessToken, response);
        }
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void createResponseRefreshToken(JwtTokenStorageMethod method) {
        // Arrange
        this.mockProperties(method);
        var refreshAccessToken = JwtRefreshToken.random();
        var response = mock(HttpServletResponse.class);

        // Act
        this.componentUnderTest.createResponseRefreshToken(refreshAccessToken, response);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).createResponseRefreshToken(refreshAccessToken, response);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).createResponseRefreshToken(refreshAccessToken, response);
        }
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void readCsrfToken(JwtTokenStorageMethod method) throws CsrfTokenNotFoundException {
        // Arrange
        this.mockProperties(method);
        var request = mock(HttpServletRequest.class);

        // Act
        this.componentUnderTest.readCsrfToken(request);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).readCsrfToken(request);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).readCsrfToken(request);
        }
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void readRequestAccessToken(JwtTokenStorageMethod method) throws AccessTokenNotFoundException {
        // Arrange
        this.mockProperties(method);
        var request = mock(HttpServletRequest.class);

        // Act
        this.componentUnderTest.readRequestAccessToken(request);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).readRequestAccessToken(request);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).readRequestAccessToken(request);
        }
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void readRequestAccessTokenOnWebsocketHandshake(JwtTokenStorageMethod method) throws AccessTokenNotFoundException {
        // Arrange
        this.mockProperties(method);
        var request = mock(HttpServletRequest.class);

        // Act
        this.componentUnderTest.readRequestAccessTokenOnWebsocketHandshake(request);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).readRequestAccessTokenOnWebsocketHandshake(request);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).readRequestAccessTokenOnWebsocketHandshake(request);
        }
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void readRequestRefreshToken(JwtTokenStorageMethod method) throws RefreshTokenNotFoundException {
        // Arrange
        this.mockProperties(method);
        var request = mock(HttpServletRequest.class);

        // Act
        this.componentUnderTest.readRequestRefreshToken(request);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).readRequestRefreshToken(request);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).readRequestRefreshToken(request);
        }
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void readRequestRefreshTokenOnWebsocketHandshake(JwtTokenStorageMethod method) throws RefreshTokenNotFoundException {
        // Arrange
        this.mockProperties(method);
        var request = mock(HttpServletRequest.class);

        // Act
        this.componentUnderTest.readRequestRefreshTokenOnWebsocketHandshake(request);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).readRequestRefreshTokenOnWebsocketHandshake(request);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).readRequestRefreshTokenOnWebsocketHandshake(request);
        }
    }

    @ParameterizedTest
    @MethodSource("jwtTokenStoragesArgs")
    void clearTokens(JwtTokenStorageMethod method) {
        // Arrange
        this.mockProperties(method);
        var response = mock(HttpServletResponse.class);

        // Act
        this.componentUnderTest.clearTokens(response);

        // Assert
        if (method.isCookies()) {
            verify(this.tokensCookiesProvider).clearTokens(response);
        }
        if (method.isHeaders()) {
            verify(this.tokensHeadersProvider).clearTokens(response);
        }
    }

    // =================================================================================================================
    // PRIVATE METHODS
    // =================================================================================================================
    private void mockProperties(JwtTokenStorageMethod method) {
        when(this.applicationFrameworkProperties.getSecurityJwtConfigs()).thenReturn(
                new SecurityJwtConfigs(
                        AuthoritiesConfigs.testsHardcoded(),
                        CookiesConfigs.testsHardcoded(),
                        EssenceConfigs.testsHardcoded(),
                        IncidentsConfigs.testsHardcoded(),
                        new JwtTokensConfigs(
                                "TECH1",
                                method,
                                new JwtToken(new TimeAmount(30L, SECONDS), "ajwt", "T-AJWT"),
                                new JwtToken(new TimeAmount(12L, HOURS), "rjwt", "T-RJWT")
                        ),
                        LoggingConfigs.testsHardcoded(),
                        SessionConfigs.testsHardcoded(),
                        UsersEmailsConfigs.testsHardcoded()
                )
        );
    }
}
