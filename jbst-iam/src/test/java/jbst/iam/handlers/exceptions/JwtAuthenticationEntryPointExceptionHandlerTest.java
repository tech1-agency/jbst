package jbst.iam.handlers.exceptions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbst.iam.domain.events.EventAuthenticationLoginFailure;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.configurations.TestConfigurationHandlers;
import jbst.iam.utils.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.http.requests.IPAddress;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static jbst.foundation.domain.exceptions.ExceptionEntityType.ERROR;
import static jbst.foundation.utilities.random.RandomUtility.randomString;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class JwtAuthenticationEntryPointExceptionHandlerTest {

    @Configuration
    @Import({
            TestConfigurationHandlers.class
    })
    static class ContextConfiguration {

    }

    private final SecurityJwtEventsPublisher securityJwtPublisher;
    private final HttpRequestUtils httpRequestUtils;
    private final ObjectMapper objectMapper;

    private final JwtAuthenticationEntryPointExceptionHandler componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.securityJwtPublisher,
                this.httpRequestUtils
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.securityJwtPublisher,
                this.httpRequestUtils
        );
    }

    @Test
    void commenceTest() throws IOException {
        // Arrange
        var response = mock(HttpServletResponse.class);
        var printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);
        var request = mock(HttpServletRequest.class);
        var authenticationException = mock(AuthenticationException.class);
        when(authenticationException.getMessage()).thenReturn(randomString());

        // Act
        this.componentUnderTest.commence(request, response, authenticationException);

        // Assert
        assertAndVerifyBasicCommence(
                request,
                response,
                printWriter,
                authenticationException
        );
    }

    @Test
    void commenceBadCredentialsExceptionNotCachedEndpointTest() throws IOException {
        // Arrange
        var response = mock(HttpServletResponse.class);
        var printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);
        var request = mock(HttpServletRequest.class);
        var badCredentialsException = mock(BadCredentialsException.class);
        when(badCredentialsException.getMessage()).thenReturn(randomString());
        when(this.httpRequestUtils.isCachedEndpoint(request)).thenReturn(false);

        // Act
        this.componentUnderTest.commence(request, response, badCredentialsException);

        // Assert
        assertAndVerifyBasicCommence(
                request,
                response,
                printWriter,
                badCredentialsException
        );
        verify(this.httpRequestUtils).isCachedEndpoint(request);
    }

    @Test
    void commenceBadCredentialsExceptionCachedEndpointTest() throws IOException {
        // Arrange
        var response = mock(HttpServletResponse.class);
        var printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);
        var request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(IPAddress.localhost().value());
        var badCredentialsException = mock(BadCredentialsException.class);
        when(badCredentialsException.getMessage()).thenReturn(randomString());
        when(this.httpRequestUtils.isCachedEndpoint(request)).thenReturn(true);
        var payload = objectMapper.writeValueAsString(
                Map.of(
                        "username", Username.hardcoded().value(),
                        "password", Password.hardcoded().value()
                )
        );
        when(this.httpRequestUtils.getCachedPayload(request)).thenReturn(payload);

        // Act
        this.componentUnderTest.commence(request, response, badCredentialsException);

        // Assert
        verify(request).getHeader("X-Forwarded-For");
        verify(request).getHeader("User-Agent");
        assertAndVerifyBasicCommence(
                request,
                response,
                printWriter,
                badCredentialsException
        );
        verify(this.httpRequestUtils).isCachedEndpoint(request);
        verify(this.httpRequestUtils).getCachedPayload(request);
        var eventAC = ArgumentCaptor.forClass(EventAuthenticationLoginFailure.class);
        verify(this.securityJwtPublisher).publishAuthenticationLoginFailure(eventAC.capture());
        assertThat(eventAC.getValue().username()).isEqualTo(Username.hardcoded());
        assertThat(eventAC.getValue().password()).isEqualTo(Password.hardcoded());
        assertThat(eventAC.getValue().ipAddress()).isEqualTo(IPAddress.localhost());

    }

    // =================================================================================================================
    // PRIVATE METHODS
    // =================================================================================================================
    @SuppressWarnings("unchecked")
    private void assertAndVerifyBasicCommence(
            HttpServletRequest request,
            HttpServletResponse response,
            PrintWriter printWriter,
            Exception exception
    ) throws IOException {
        var jsonAC = ArgumentCaptor.forClass(String.class);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).getWriter();
        verify(printWriter).write(jsonAC.capture());
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
        HashMap<String, Object> json = this.objectMapper.readValue(jsonAC.getValue(), typeRef);
        assertThat(json)
                .hasSize(3)
                .containsKeys("exceptionEntityType", "attributes", "timestamp")
                .containsEntry("exceptionEntityType", ERROR.toString());
        var attributes = (Map<String, Object>) json.get("attributes");
        assertThat(attributes)
                .containsEntry("shortMessage", exception.getMessage())
                .containsEntry("fullMessage", exception.getMessage());
        verify(exception, times(4)).getMessage();
        verifyNoMoreInteractions(
                request,
                response,
                exception
        );
    }
}
