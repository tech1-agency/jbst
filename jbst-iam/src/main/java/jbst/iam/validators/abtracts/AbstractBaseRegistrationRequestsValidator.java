package jbst.iam.validators.abtracts;

import jbst.foundation.domain.exceptions.authentication.RegistrationException;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0Failure;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1Failure;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.domain.events.EventRegistration0Failure;
import jbst.iam.domain.events.EventRegistration1Failure;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.InvitationsRepository;
import jbst.iam.repositories.UsersRepository;
import jbst.iam.validators.BaseRegistrationRequestsValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static java.util.Objects.nonNull;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.entityAlreadyUsed;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.entityNotFound;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractBaseRegistrationRequestsValidator implements BaseRegistrationRequestsValidator {

    // Publishers
    protected final SecurityJwtEventsPublisher securityJwtPublisher;
    protected final SecurityJwtIncidentsPublisher securityJwtIncidentPublisher;
    // Repositories
    protected final InvitationsRepository invitationsRepository;
    protected final UsersRepository usersRepository;

    @Override
    public void validateRegistrationRequest0(RequestUserRegistration0 request) throws RegistrationException {
        request.assertPasswordsOrThrow();
        var existsByUsername = this.usersRepository.existsByUsername(request.username());
        if (existsByUsername) {
            var message = entityAlreadyUsed("Username", request.username().value());
            this.securityJwtPublisher.publishRegistration0Failure(
                    new EventRegistration0Failure(
                            request.email(),
                            request.username(),
                            message
                    )
            );
            this.securityJwtIncidentPublisher.publishRegistration0Failure(
                    new IncidentRegistration0Failure(
                            request.email(),
                            request.username(),
                            message
                    )
            );
            throw new RegistrationException(message);
        }
        var existsByEmail = this.usersRepository.existsByEmail(request.email());
        if (existsByEmail) {
            var message = entityAlreadyUsed("Email", request.email().value());
            this.securityJwtPublisher.publishRegistration0Failure(
                    new EventRegistration0Failure(
                            request.email(),
                            request.username(),
                            message
                    )
            );
            this.securityJwtIncidentPublisher.publishRegistration0Failure(
                    new IncidentRegistration0Failure(
                            request.email(),
                            request.username(),
                            message
                    )
            );
            throw new RegistrationException(message);
        }
    }

    @Override
    public void validateRegistrationRequest1(RequestUserRegistration1 request) throws RegistrationException {
        request.assertPasswordsOrThrow();
        var user = this.usersRepository.findByUsernameAsJwtUserOrNull(request.username());
        if (nonNull(user)) {
            var message = entityAlreadyUsed("Username", request.username().value());
            this.securityJwtPublisher.publishRegistration1Failure(
                    EventRegistration1Failure.of(
                            request.username(),
                            request.code(),
                            message
                    )
            );
            this.securityJwtIncidentPublisher.publishRegistration1Failure(
                    IncidentRegistration1Failure.of(
                            request.username(),
                            request.code(),
                            message
                    )
            );
            throw new RegistrationException(message);
        }

        var invitation = this.invitationsRepository.findByCodeAsAny(request.code());
        if (nonNull(invitation)) {
            if (nonNull(invitation.invited())) {
                var message = entityAlreadyUsed("Code", invitation.code());
                this.securityJwtPublisher.publishRegistration1Failure(
                        new EventRegistration1Failure(
                                request.username(),
                                request.code(),
                                invitation.owner(),
                                message
                        )
                );
                this.securityJwtIncidentPublisher.publishRegistration1Failure(
                        new IncidentRegistration1Failure(
                                request.username(),
                                request.code(),
                                invitation.owner(),
                                message
                        )
                );
                throw new RegistrationException(message);
            }
        } else {
            var exception = entityNotFound("Code", request.code());
            this.securityJwtPublisher.publishRegistration1Failure(
                    EventRegistration1Failure.of(
                            request.username(),
                            request.code(),
                            exception
                    )
            );
            this.securityJwtIncidentPublisher.publishRegistration1Failure(
                    IncidentRegistration1Failure.of(
                            request.username(),
                            request.code(),
                            exception
                    )
            );
            throw new RegistrationException(exception);
        }
    }
}
