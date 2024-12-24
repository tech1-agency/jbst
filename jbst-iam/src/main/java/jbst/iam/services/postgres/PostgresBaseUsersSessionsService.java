package jbst.iam.services.postgres;

import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.postgres.PostgresUsersSessionsRepository;
import jbst.iam.services.abstracts.AbstractBaseUsersSessionsService;
import jbst.iam.utils.SecurityJwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jbst.foundation.utils.UserMetadataUtils;

@Slf4j
@Service
public class PostgresBaseUsersSessionsService extends AbstractBaseUsersSessionsService {

    @Autowired
    public PostgresBaseUsersSessionsService(
            SecurityJwtEventsPublisher securityJwtEventsPublisher,
            PostgresUsersSessionsRepository usersSessionsRepository,
            UserMetadataUtils userMetadataUtils,
            SecurityJwtTokenUtils securityJwtTokenUtils
    ) {
        super(
                securityJwtEventsPublisher,
                usersSessionsRepository,
                userMetadataUtils,
                securityJwtTokenUtils
        );
    }
}
