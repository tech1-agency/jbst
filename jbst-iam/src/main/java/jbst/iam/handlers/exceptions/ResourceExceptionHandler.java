package jbst.iam.handlers.exceptions;

import jbst.foundation.domain.exceptions.ExceptionEntity;
import jbst.foundation.domain.exceptions.ExceptionEntityType;
import jbst.foundation.domain.exceptions.authentication.RegistrationException;
import jbst.foundation.domain.exceptions.base.TooManyRequestsException;
import jbst.foundation.domain.exceptions.cookies.CookieNotFoundException;
import jbst.foundation.domain.exceptions.tokens.*;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static java.util.Objects.isNull;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.contactDevelopmentTeam;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.unexpectedErrorOccurred;

// WARNING: @Order by default uses Ordered.LOWEST_PRECEDENCE
@Slf4j
@Order
@ControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResourceExceptionHandler {

    private final IncidentPublisher incidentPublisher;

    // =================================================================================================================
    // DEDICATED EXCEPTIONS
    // =================================================================================================================

    @ExceptionHandler({
            RegistrationException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionEntity> registerException(RegistrationException ex) {
        var response = new ExceptionEntity(
                ExceptionEntityType.ERROR,
                contactDevelopmentTeam("Registration Failure"),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
            UserTokenValidationException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionEntity> userEmailTokenValidationException(UserTokenValidationException ex) {
        var response = new ExceptionEntity(
                ExceptionEntityType.ERROR,
                contactDevelopmentTeam("Token Validation Failure"),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionEntity> methodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return new ResponseEntity<>(new ExceptionEntity(ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
            TooManyRequestsException.class
    })
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseEntity<ExceptionEntity> tooManyRequestsException(TooManyRequestsException ignoredEx) {
        var response = new ExceptionEntity(
                ExceptionEntityType.ERROR,
                "Too many requests, please wait",
                "Too many requests, please wait"
        );
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    // =================================================================================================================
    // GROUPED EXCEPTIONS
    // =================================================================================================================

    @ExceptionHandler({
            CookieNotFoundException.class,
            AccessTokenNotFoundException.class,
            AccessTokenInvalidException.class,
            AccessTokenExpiredException.class,
            AccessTokenDbNotFoundException.class,
            RefreshTokenNotFoundException.class,
            RefreshTokenInvalidException.class,
            RefreshTokenExpiredException.class,
            RefreshTokenDbNotFoundException.class,
            TokenUnauthorizedException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ExceptionEntity> unauthorizedExceptions(Exception ex) {
        return new ResponseEntity<>(new ExceptionEntity(ex), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({
            AccessDeniedException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ExceptionEntity> forbiddenExceptions(Exception ex) {
        return new ResponseEntity<>(new ExceptionEntity(ex), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            HttpMessageConversionException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ExceptionEntity> badRequestExceptions(Exception ex) {
        var response = new ExceptionEntity(
                ExceptionEntityType.ERROR,
                contactDevelopmentTeam("Malformed request syntax"),
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionEntity> internalServerError(Exception ex) {
        return new ResponseEntity<>(new ExceptionEntity(ex), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
            Exception.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ExceptionEntity> generalException(Exception ex) {
        if (isNull(ex) || isNull(ex.getMessage())) {
            var response = new ExceptionEntity(
                    ExceptionEntityType.ERROR,
                    unexpectedErrorOccurred(),
                    unexpectedErrorOccurred()
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            LOGGER.error("Unexpected error occurred", ex);
            var response = new ExceptionEntity(
                    ExceptionEntityType.ERROR,
                    unexpectedErrorOccurred(),
                    ex.getMessage()
            );
            this.incidentPublisher.publishThrowable(ex);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
