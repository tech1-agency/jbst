package jbst.iam.tokens.providers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbst.foundation.domain.exceptions.tokens.AccessTokenNotFoundException;
import jbst.foundation.domain.exceptions.tokens.CsrfTokenNotFoundException;
import jbst.foundation.domain.exceptions.tokens.RefreshTokenNotFoundException;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.jwt.RequestRefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@Qualifier("tokenHeadersProvider")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TokenHeadersProvider implements TokenProvider {

    // Properties
    private final JbstProperties jbstProperties;

    @Override
    public void createResponseAccessToken(JwtAccessToken jwtAccessToken, HttpServletResponse response) {
        var headerKey = this.jbstProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getAccessToken().getHeaderKey();
        response.addHeader(headerKey, jwtAccessToken.value());
    }

    @Override
    public void createResponseRefreshToken(JwtRefreshToken jwtRefreshToken, HttpServletResponse response) {
        var headerKey = this.jbstProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getRefreshToken().getHeaderKey();
        response.addHeader(headerKey, jwtRefreshToken.value());
    }

    @Override
    public DefaultCsrfToken readCsrfToken(HttpServletRequest request) throws CsrfTokenNotFoundException {
        var csrfConfigs = this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().getCsrfConfigs();
        // WARNING: development workaround to read request query parameters instead of request headers
        var header = request.getParameter(csrfConfigs.getTokenKey());
        if (nonNull(header)) {
            return new DefaultCsrfToken(csrfConfigs.getHeaderName(), csrfConfigs.getParameterName(), header);
        } else {
            throw new CsrfTokenNotFoundException();
        }
    }

    @Override
    public RequestAccessToken readRequestAccessToken(HttpServletRequest request) throws AccessTokenNotFoundException {
        var headerKey = this.jbstProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getAccessToken().getHeaderKey();
        var header = request.getHeader(headerKey);
        if (nonNull(header)) {
            return new RequestAccessToken(header);
        } else {
            throw new AccessTokenNotFoundException();
        }
    }

    @Override
    public RequestAccessToken readRequestAccessTokenOnWebsocketHandshake(HttpServletRequest request) throws AccessTokenNotFoundException {
        var headerKey = this.jbstProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getAccessToken().getHeaderKey();
        // WARNING: development workaround to read request query parameters instead of request headers
        var header = request.getParameter(headerKey);
        if (nonNull(header)) {
            return new RequestAccessToken(header);
        } else {
            throw new AccessTokenNotFoundException();
        }
    }

    @Override
    public RequestRefreshToken readRequestRefreshToken(HttpServletRequest request) throws RefreshTokenNotFoundException {
        var headerKey = this.jbstProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getRefreshToken().getHeaderKey();
        var header = request.getHeader(headerKey);
        if (nonNull(header)) {
            return new RequestRefreshToken(header);
        } else {
            throw new RefreshTokenNotFoundException();
        }
    }

    @Override
    public RequestRefreshToken readRequestRefreshTokenOnWebsocketHandshake(HttpServletRequest request) throws RefreshTokenNotFoundException {
        var headerKey = this.jbstProperties.getSecurityJwtConfigs().getJwtTokensConfigs().getRefreshToken().getHeaderKey();
        var header = request.getParameter(headerKey);
        if (nonNull(header)) {
            return new RequestRefreshToken(header);
        } else {
            throw new RefreshTokenNotFoundException();
        }
    }

    @Override
    public void clearTokens(HttpServletResponse response) {
        // headers stored on front-end
    }
}
