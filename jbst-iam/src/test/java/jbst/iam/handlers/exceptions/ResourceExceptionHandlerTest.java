package jbst.iam.handlers.exceptions;

import jbst.foundation.domain.exceptions.tokens.*;
import jbst.iam.configurations.TestConfigurationHandlers;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.exceptions.authentication.RegistrationException;
import jbst.foundation.domain.exceptions.cookies.CookieNotFoundException;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static jbst.foundation.domain.exceptions.ExceptionEntityType.ERROR;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.contactDevelopmentTeam;
import static jbst.foundation.utilities.random.RandomUtility.randomString;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ResourceExceptionHandlerTest {

    @Configuration
    @Import({
            TestConfigurationHandlers.class
    })
    static class ContextConfiguration {

    }

    private final ResourceExceptionHandler componentUnderTest;

    private static Stream<Arguments> unauthorizedResponseErrorMessageTest() {
        return Stream.of(
                Arguments.of(new CookieNotFoundException(randomString())),
                Arguments.of(new AccessTokenNotFoundException()),
                Arguments.of(new AccessTokenInvalidException()),
                Arguments.of(new AccessTokenExpiredException(Username.random())),
                Arguments.of(new AccessTokenDbNotFoundException(Username.random())),
                Arguments.of(new RefreshTokenNotFoundException()),
                Arguments.of(new RefreshTokenInvalidException()),
                Arguments.of(new RefreshTokenExpiredException(Username.random())),
                Arguments.of(new RefreshTokenDbNotFoundException(Username.random())),
                Arguments.of(new TokenUnauthorizedException(randomString()))
        );
    }

    private static Stream<Arguments> forbiddenResponseErrorMessageTest() {
        return Stream.of(
                Arguments.of(new AccessDeniedException(randomString()))
        );
    }

    @ParameterizedTest
    @MethodSource("unauthorizedResponseErrorMessageTest")
    void unauthorizedResponseErrorMessageTest(Exception exception) {
        // Act
        var response = this.componentUnderTest.unauthorizedExceptions(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getExceptionEntityType()).isEqualTo(ERROR);
        assertThat(response.getBody().getAttributes())
                .containsEntry("shortMessage", exception.getMessage())
                .containsEntry("fullMessage", exception.getMessage());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @MethodSource("forbiddenResponseErrorMessageTest")
    void forbiddenExceptionsTest(Exception exception) {
        // Act
        var response = this.componentUnderTest.forbiddenExceptions(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getExceptionEntityType()).isEqualTo(ERROR);
        assertThat(response.getBody().getAttributes())
                .containsEntry("shortMessage", exception.getMessage())
                .containsEntry("fullMessage", exception.getMessage());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void registrationExceptionTest() {
        // Arrange
        var message = randomString();
        var exception = new RegistrationException(message);

        // Act
        var response = this.componentUnderTest.registerException(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getExceptionEntityType()).isEqualTo(ERROR);
        assertThat(response.getBody().getAttributes())
                .containsEntry("shortMessage", contactDevelopmentTeam("Registration Failure"))
                .containsEntry("fullMessage", exception.getMessage());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void internalServerErrorTest() {
        // Arrange
        var message = randomString();
        var exception = new IllegalArgumentException(message);

        // Act
        var response = this.componentUnderTest.internalServerError(exception);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getExceptionEntityType()).isEqualTo(ERROR);
        assertThat(response.getBody().getAttributes())
                .containsEntry("shortMessage", exception.getMessage())
                .containsEntry("fullMessage", exception.getMessage());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
