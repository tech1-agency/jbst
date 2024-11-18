package jbst.iam.tokens.providers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Service;
import tech1.framework.foundation.domain.exceptions.cookies.CookieNotFoundException;
import tech1.framework.foundation.domain.exceptions.tokens.AccessTokenNotFoundException;
import tech1.framework.foundation.domain.exceptions.tokens.CsrfTokenNotFoundException;
import tech1.framework.foundation.domain.exceptions.tokens.RefreshTokenNotFoundException;
import tech1.framework.foundation.domain.properties.ApplicationFrameworkProperties;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.jwt.RequestRefreshToken;

import static tech1.framework.foundation.utilities.http.HttpCookieUtility.*;
import static tech1.framework.foundation.utilities.numbers.LongUtility.toIntExactOrZeroOnOverflow;

@Slf4j
@Service
@Qualifier("tokenCookiesProvider")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenCookiesProvider implements TokenProvider {

    // Properties
    private final ApplicationFrameworkProperties applicationFrameworkProperties;

    @Override
    public void createResponseAccessToken(JwtAccessToken jwtAccessToken, HttpServletResponse response) {
        var securityJwtConfigs = this.applicationFrameworkProperties.getSecurityJwtConfigs();
        var accessTokenConfiguration = securityJwtConfigs.getJwtTokensConfigs().getAccessToken();
        var jwtAccessTokenCookieCreationLatency = securityJwtConfigs.getCookiesConfigs().getJwtAccessTokenCookieCreationLatency();
        var maxAge = accessTokenConfiguration.getExpiration().getTimeAmount().toSeconds() - jwtAccessTokenCookieCreationLatency.getTimeAmount().toSeconds();

        var cookie =  createCookie(
                accessTokenConfiguration.getCookieKey(),
                jwtAccessToken.value(),
                securityJwtConfigs.getCookiesConfigs().getDomain(),
                true,
                toIntExactOrZeroOnOverflow(maxAge)
        );

        response.addCookie(cookie);
    }

    @Override
    public void createResponseRefreshToken(JwtRefreshToken jwtRefreshToken, HttpServletResponse response) {
        var securityJwtConfigs = this.applicationFrameworkProperties.getSecurityJwtConfigs();
        var refreshTokenConfiguration = securityJwtConfigs.getJwtTokensConfigs().getRefreshToken();

        var cookie = createCookie(
                refreshTokenConfiguration.getCookieKey(),
                jwtRefreshToken.value(),
                securityJwtConfigs.getCookiesConfigs().getDomain(),
                true,
                toIntExactOrZeroOnOverflow(refreshTokenConfiguration.getExpiration().getTimeAmount().toSeconds())
        );

        response.addCookie(cookie);
    }

    @Override
    public DefaultCsrfToken readCsrfToken(HttpServletRequest request) throws CsrfTokenNotFoundException {
        try {
            var csrfConfigs = this.applicationFrameworkProperties.getSecurityJwtWebsocketsConfigs().getCsrfConfigs();
            // WARNING: security concerns? based on https://github.com/sockjs/sockjs-node#authorisation
            // GitHub issue: https://github.com/sockjs/sockjs-client/issues/196
            var csrfCookie = readCookie(request, csrfConfigs.getTokenKey());
            return new DefaultCsrfToken(csrfConfigs.getHeaderName(), csrfConfigs.getParameterName(), csrfCookie);
        } catch (CookieNotFoundException ex) {
            throw new CsrfTokenNotFoundException();
        }
    }

    @Override
    public RequestAccessToken readRequestAccessToken(HttpServletRequest request) throws AccessTokenNotFoundException {
        try {
            var accessToken = this.applicationFrameworkProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getAccessToken();
            var cookie = readCookie(request, accessToken.getCookieKey());
            return new RequestAccessToken(cookie);
        } catch (CookieNotFoundException ex) {
            throw new AccessTokenNotFoundException();
        }
    }

    @Override
    public RequestAccessToken readRequestAccessTokenOnWebsocketHandshake(HttpServletRequest request) throws AccessTokenNotFoundException {
        return this.readRequestAccessToken(request);
    }

    @Override
    public RequestRefreshToken readRequestRefreshToken(HttpServletRequest request) throws RefreshTokenNotFoundException {
        try {
            var refreshToken = this.applicationFrameworkProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getRefreshToken();
            var cookie = readCookie(request, refreshToken.getCookieKey());
            return new RequestRefreshToken(cookie);
        } catch (CookieNotFoundException ex) {
            throw new RefreshTokenNotFoundException();
        }
    }

    @Override
    public RequestRefreshToken readRequestRefreshTokenOnWebsocketHandshake(HttpServletRequest request) throws RefreshTokenNotFoundException {
        return this.readRequestRefreshToken(request);
    }

    @Override
    public void clearTokens(HttpServletResponse response) {
        var securityJwtConfigs = this.applicationFrameworkProperties.getSecurityJwtConfigs();
        var cookiesConfigs = securityJwtConfigs.getCookiesConfigs();
        var accessToken = securityJwtConfigs.getJwtTokensConfigs().getAccessToken();
        var refreshToken = securityJwtConfigs.getJwtTokensConfigs().getRefreshToken();

        response.addCookie(createNullCookie(accessToken.getCookieKey(), cookiesConfigs.getDomain()));
        response.addCookie(createNullCookie(refreshToken.getCookieKey(), cookiesConfigs.getDomain()));
    }
}
