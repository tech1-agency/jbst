package jbst.iam.validators.mongodb;

import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.mongodb.MongoInvitationsRepository;
import jbst.iam.repositories.mongodb.MongoUsersRepository;
import jbst.iam.validators.abtracts.AbstractBaseRegistrationRequestsValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoBaseRegistrationRequestsValidator extends AbstractBaseRegistrationRequestsValidator {

    @Autowired
    public MongoBaseRegistrationRequestsValidator(
            SecurityJwtEventsPublisher securityJwtEventsPublisher,
            SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher,
            MongoInvitationsRepository invitationsRepository,
            MongoUsersRepository usersRepository
    ) {
        super(
                securityJwtEventsPublisher,
                securityJwtIncidentsPublisher,
                invitationsRepository,
                usersRepository
        );
    }
}
