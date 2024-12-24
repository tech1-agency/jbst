package jbst.iam.sessions;

import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.mongodb.MongoUsersSessionsRepository;
import jbst.iam.services.mongodb.MongoBaseUsersSessionsService;

public class MongoSessionRegistry extends AbstractSessionRegistry {

    public MongoSessionRegistry(
            SecurityJwtEventsPublisher securityJwtEventsPublisher,
            SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher,
            MongoBaseUsersSessionsService baseUsersSessionsService,
            MongoUsersSessionsRepository usersSessionsRepository
    ) {
        super(
                securityJwtEventsPublisher,
                securityJwtIncidentsPublisher,
                baseUsersSessionsService,
                usersSessionsRepository
        );
    }
}
