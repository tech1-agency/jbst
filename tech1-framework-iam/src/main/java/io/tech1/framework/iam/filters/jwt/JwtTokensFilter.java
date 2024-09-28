package io.tech1.framework.iam.filters.jwt;

import io.tech1.framework.iam.domain.sessions.Session;
import io.tech1.framework.iam.filters.jwt_extension.JwtTokensFilterExtension;
import io.tech1.framework.iam.handlers.exceptions.JwtAccessDeniedExceptionHandler;
import io.tech1.framework.iam.services.TokensService;
import io.tech1.framework.iam.sessions.SessionRegistry;
import io.tech1.framework.iam.tokens.facade.TokensProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech1.framework.foundation.domain.exceptions.tokens.*;

import java.io.IOException;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtTokensFilter extends OncePerRequestFilter {

    // Session
    private final SessionRegistry sessionRegistry;
    // Services
    private final TokensService tokensService;
    // Tokens
    private final TokensProvider tokensProvider;
    // Extension
    private final JwtTokensFilterExtension jwtTokensFilterExtension;
    // Handlers
    private final JwtAccessDeniedExceptionHandler jwtAccessDeniedExceptionHandler;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest req, @NotNull HttpServletResponse res, @NotNull FilterChain chain) throws ServletException, IOException {
        try {
            var cookieAccessToken = this.tokensProvider.readRequestAccessToken(req);
            var cookieRefreshToken = this.tokensProvider.readRequestRefreshToken(req);
            var user = this.tokensService.getJwtUserByAccessTokenOrThrow(cookieAccessToken, cookieRefreshToken);

            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            this.sessionRegistry.register(new Session(user.username(), cookieAccessToken.getJwtAccessToken(), cookieRefreshToken.getJwtRefreshToken()));

            this.jwtTokensFilterExtension.doFilter(req);

            chain.doFilter(req, res);
        } catch (
                AccessTokenNotFoundException |
                AccessTokenExpiredException ex
        ) {
            // distinguish authenticated vs. anonymous/permitAll endpoints
            chain.doFilter(req, res);
        } catch (
                RefreshTokenNotFoundException |
                AccessTokenInvalidException |
                RefreshTokenInvalidException |
                AccessTokenDbNotFoundException |
                TokenExtensionUnauthorizedException ex
        ) {
            LOGGER.debug("JWT unauthorized request → clear cookies. Message: {}", ex.getMessage());
            this.tokensProvider.clearTokens(res);
            res.sendError(HttpStatus.UNAUTHORIZED.value());
        } catch (TokenExtensionAccessDeniedException ex) {
            LOGGER.debug("JWT forbidden request → clear cookies. Message: {}", ex.getMessage());
            this.tokensProvider.clearTokens(res);
            this.jwtAccessDeniedExceptionHandler.handle(req, res, new AccessDeniedException(ex.getMessage()));
        }
    }
}
