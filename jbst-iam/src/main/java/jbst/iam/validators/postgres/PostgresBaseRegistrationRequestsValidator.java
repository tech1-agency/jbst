package jbst.iam.validators.postgres;

import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.postgres.PostgresInvitationsRepository;
import jbst.iam.repositories.postgres.PostgresUsersRepository;
import jbst.iam.validators.abtracts.AbstractBaseRegistrationRequestsValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostgresBaseRegistrationRequestsValidator extends AbstractBaseRegistrationRequestsValidator {

    @Autowired
    public PostgresBaseRegistrationRequestsValidator(
            SecurityJwtEventsPublisher securityJwtEventsPublisher,
            SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher,
            PostgresInvitationsRepository invitationsRepository,
            PostgresUsersRepository usersRepository
    ) {
        super(
                securityJwtEventsPublisher,
                securityJwtIncidentsPublisher,
                invitationsRepository,
                usersRepository
        );
    }
}
