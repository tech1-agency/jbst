package jbst.iam.validators.abstracts;

import jbst.foundation.incidents.domain.registration.IncidentRegistration0Failure;
import jbst.iam.domain.db.Invitation;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.domain.events.EventRegistration0Failure;
import jbst.iam.domain.events.EventRegistration1Failure;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.InvitationsRepository;
import jbst.iam.repositories.UsersRepository;
import jbst.iam.configurations.TestConfigurationValidators;
import jbst.iam.validators.BaseRegistrationRequestsValidator;
import jbst.iam.validators.abtracts.AbstractBaseRegistrationRequestsValidator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import jbst.foundation.domain.exceptions.authentication.RegistrationException;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1Failure;
import jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AbstractBaseRegistrationRequestsValidatorTest {

    @Configuration
    @Import({
            TestConfigurationValidators.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        private final InvitationsRepository invitationsRepository;
        private final UsersRepository usersRepository;
        private final SecurityJwtEventsPublisher securityJwtEventsPublisher;
        private final SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher;

        @Bean
        BaseRegistrationRequestsValidator baseInvitationRequestsValidator() {
            return new AbstractBaseRegistrationRequestsValidator(
                    this.securityJwtEventsPublisher,
                    this.securityJwtIncidentsPublisher,
                    this.invitationsRepository,
                    this.usersRepository
            ) {};
        }
    }

    private final SecurityJwtEventsPublisher securityJwtEventsPublisher;
    private final SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher;
    private final InvitationsRepository invitationsRepository;
    private final UsersRepository usersRepository;

    private final BaseRegistrationRequestsValidator componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.securityJwtEventsPublisher,
                this.securityJwtIncidentsPublisher,
                this.invitationsRepository,
                this.usersRepository
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.securityJwtIncidentsPublisher,
                this.securityJwtEventsPublisher,
                this.invitationsRepository,
                this.usersRepository
        );
    }

    @Test
    void validateRegistrationRequest0UsernameAlreadyUsedTest() {
        // Arrange
        var request = RequestUserRegistration0.hardcoded();
        when(this.usersRepository.existsByUsername(request.username())).thenReturn(true);

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.validateRegistrationRequest0(request));

        // Assert
        var exception = ExceptionsMessagesUtility.entityAlreadyUsed("Username", request.username().value());
        assertThat(throwable)
                .isInstanceOf(RegistrationException.class)
                .hasMessage(exception);
        verify(this.usersRepository).existsByUsername(request.username());
        verify(this.securityJwtEventsPublisher).publishRegistration0Failure(
                new EventRegistration0Failure(
                        request.email(),
                        request.username(),
                        exception
                )
        );
        verify(this.securityJwtIncidentsPublisher).publishRegistration0Failure(
                new IncidentRegistration0Failure(
                        request.email(),
                        request.username(),
                        exception
                )
        );
    }

    @Test
    void validateRegistrationRequest0EmailAlreadyUsedTest() {
        // Arrange
        var request = RequestUserRegistration0.hardcoded();
        when(this.usersRepository.existsByUsername(request.username())).thenReturn(false);
        when(this.usersRepository.existsByEmail(request.email())).thenReturn(true);

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.validateRegistrationRequest0(request));

        // Assert
        var exception = ExceptionsMessagesUtility.entityAlreadyUsed("Email", request.email().value());
        assertThat(throwable)
                .isInstanceOf(RegistrationException.class)
                .hasMessage(exception);
        verify(this.usersRepository).existsByUsername(request.username());
        verify(this.usersRepository).existsByEmail(request.email());
        verify(this.securityJwtEventsPublisher).publishRegistration0Failure(
                new EventRegistration0Failure(
                        request.email(),
                        request.username(),
                        exception
                )
        );
        verify(this.securityJwtIncidentsPublisher).publishRegistration0Failure(
                new IncidentRegistration0Failure(
                        request.email(),
                        request.username(),
                        exception
                )
        );
    }

    @Test
    void validateRegistrationRequest0UsernameEmailFreeTest() throws RegistrationException {
        // Arrange
        var request = RequestUserRegistration0.hardcoded();
        when(this.usersRepository.existsByUsername(request.username())).thenReturn(false);
        when(this.usersRepository.existsByEmail(request.email())).thenReturn(false);

        // Act
        this.componentUnderTest.validateRegistrationRequest0(request);

        // Assert
        verify(this.usersRepository).existsByUsername(request.username());
        verify(this.usersRepository).existsByEmail(request.email());
    }

    @Test
    void validateRegistrationRequest1UsernameAlreadyUsedTest() {
        // Arrange
        var request = RequestUserRegistration1.hardcoded();
        when(this.usersRepository.findByUsernameAsJwtUserOrNull(request.username())).thenReturn(JwtUser.hardcoded());

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.validateRegistrationRequest1(request));

        // Assert
        var exception = ExceptionsMessagesUtility.entityAlreadyUsed("Username", request.username().value());
        assertThat(throwable)
                .isInstanceOf(RegistrationException.class)
                .hasMessage(exception);
        verify(this.usersRepository).findByUsernameAsJwtUserOrNull(request.username());
        verify(this.securityJwtEventsPublisher).publishRegistration1Failure(
                EventRegistration1Failure.of(
                        request.username(),
                        request.code(),
                        exception
                )
        );
        verify(this.securityJwtIncidentsPublisher).publishRegistration1Failure(
                IncidentRegistration1Failure.of(
                        request.username(),
                        request.code(),
                        exception
                )
        );
    }

    @Test
    void validateRegistrationRequest1InvitationAlreadyUsedTest() {
        // Arrange
        var request = RequestUserRegistration1.hardcoded();
        var invitation = Invitation.random();
        when(this.usersRepository.findByUsernameAsJwtUserOrNull(request.username())).thenReturn(null);
        when(this.invitationsRepository.findByCodeAsAny(request.code())).thenReturn(invitation);

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.validateRegistrationRequest1(request));

        // Assert
        var exception = ExceptionsMessagesUtility.entityAlreadyUsed("Code", invitation.code());
        assertThat(throwable)
                .isInstanceOf(RegistrationException.class)
                .hasMessage(exception);
        verify(this.usersRepository).findByUsernameAsJwtUserOrNull(request.username());
        verify(this.invitationsRepository).findByCodeAsAny(request.code());
        verify(this.securityJwtEventsPublisher).publishRegistration1Failure(
                new EventRegistration1Failure(
                        request.username(),
                        request.code(),
                        invitation.owner(),
                        exception
                )
        );
        verify(this.securityJwtIncidentsPublisher).publishRegistration1Failure(
                new IncidentRegistration1Failure(
                        request.username(),
                        request.code(),
                        invitation.owner(),
                        exception
                )
        );
    }

    @Test
    void validateRegistrationRequest1NoInvitationTest() {
        // Arrange
        var request = RequestUserRegistration1.hardcoded();
        var username = request.username();
        var invitation = request.code();
        when(this.usersRepository.findByUsernameAsJwtUserOrNull(username)).thenReturn(null);
        when(this.invitationsRepository.findByCodeAsAny(invitation)).thenReturn(null);

        // Act
        var throwable = catchThrowable(() -> this.componentUnderTest.validateRegistrationRequest1(request));

        // Assert
        var exception = ExceptionsMessagesUtility.entityNotFound("Code", invitation);
        assertThat(throwable)
                .isInstanceOf(RegistrationException.class)
                .hasMessage(exception);
        verify(this.usersRepository).findByUsernameAsJwtUserOrNull(username);
        verify(this.invitationsRepository).findByCodeAsAny(invitation);
        verify(this.securityJwtEventsPublisher).publishRegistration1Failure(
                EventRegistration1Failure.of(
                        username,
                        invitation,
                        exception
                )
        );
        verify(this.securityJwtIncidentsPublisher).publishRegistration1Failure(
                IncidentRegistration1Failure.of(
                        username,
                        invitation,
                        exception
                )
        );
    }

    @Test
    void validateRegistrationRequest1InvitationPresentTest() throws RegistrationException {
        // Arrange
        var request = RequestUserRegistration1.hardcoded();
        when(this.usersRepository.findByUsernameAsJwtUserOrNull(request.username())).thenReturn(null);
        when(this.invitationsRepository.findByCodeAsAny(request.code())).thenReturn(Invitation.randomNoInvited());

        // Act
        this.componentUnderTest.validateRegistrationRequest1(request);

        // Assert
        verify(this.usersRepository).findByUsernameAsJwtUserOrNull(request.username());
        verify(this.invitationsRepository).findByCodeAsAny(request.code());
    }
}
