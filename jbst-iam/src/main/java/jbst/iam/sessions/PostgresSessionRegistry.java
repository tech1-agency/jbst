package jbst.iam.sessions;

import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.postgres.PostgresUsersSessionsRepository;
import jbst.iam.services.postgres.PostgresBaseUsersSessionsService;

public class PostgresSessionRegistry extends AbstractSessionRegistry {

    public PostgresSessionRegistry(
            SecurityJwtEventsPublisher securityJwtEventsPublisher,
            SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher,
            PostgresBaseUsersSessionsService baseUsersSessionsService,
            PostgresUsersSessionsRepository usersSessionsRepository
    ) {
        super(
                securityJwtEventsPublisher,
                securityJwtIncidentsPublisher,
                baseUsersSessionsService,
                usersSessionsRepository
        );
    }
}
