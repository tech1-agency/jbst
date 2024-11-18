package jbst.iam.tokens.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.jwt.RequestRefreshToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import tech1.framework.foundation.domain.exceptions.tokens.AccessTokenNotFoundException;
import tech1.framework.foundation.domain.exceptions.tokens.CsrfTokenNotFoundException;
import tech1.framework.foundation.domain.exceptions.tokens.RefreshTokenNotFoundException;

public interface TokensProvider {
    void createResponseAccessToken(JwtAccessToken jwtAccessToken, HttpServletResponse response);
    void createResponseRefreshToken(JwtRefreshToken jwtRefreshToken, HttpServletResponse response);
    DefaultCsrfToken readCsrfToken(HttpServletRequest httpRequest) throws CsrfTokenNotFoundException;
    RequestAccessToken readRequestAccessToken(HttpServletRequest httpRequest) throws AccessTokenNotFoundException;
    RequestAccessToken readRequestAccessTokenOnWebsocketHandshake(HttpServletRequest httpRequest) throws AccessTokenNotFoundException;
    RequestRefreshToken readRequestRefreshToken(HttpServletRequest httpRequest) throws RefreshTokenNotFoundException;
    RequestRefreshToken readRequestRefreshTokenOnWebsocketHandshake(HttpServletRequest httpRequest) throws RefreshTokenNotFoundException;
    void clearTokens(HttpServletResponse httpResponse);
}
