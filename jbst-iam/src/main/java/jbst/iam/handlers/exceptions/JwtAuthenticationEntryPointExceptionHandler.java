package jbst.iam.handlers.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbst.iam.domain.dto.requests.RequestUserLogin;
import jbst.iam.domain.events.EventAuthenticationLoginFailure;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.utils.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import jbst.foundation.domain.exceptions.ExceptionEntity;
import jbst.foundation.domain.http.requests.UserAgentHeader;

import java.io.IOException;

import static jbst.foundation.utilities.http.HttpServletRequestUtility.getClientIpAddr;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class JwtAuthenticationEntryPointExceptionHandler implements AuthenticationEntryPoint {

    // Publishers
    private final SecurityJwtEventsPublisher securityJwtPublisher;
    // Utilities
    private final HttpRequestUtils httpRequestUtils;
    // JSONs
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(this.objectMapper.writeValueAsString(new ExceptionEntity(exception)));

        // in case of another endpoint to cache - extract methods like: isLoginEndpoint or isLogoutEndpoint
        if (exception instanceof BadCredentialsException && this.httpRequestUtils.isCachedEndpoint(request)) {
            var requestUserLogin = this.objectMapper.readValue(
                    this.httpRequestUtils.getCachedPayload(request),
                    RequestUserLogin.class
            );

            this.securityJwtPublisher.publishAuthenticationLoginFailure(
                    new EventAuthenticationLoginFailure(
                            requestUserLogin.username(),
                            requestUserLogin.password(),
                            getClientIpAddr(request),
                            new UserAgentHeader(request)
                    )
            );
        }
    }
}
