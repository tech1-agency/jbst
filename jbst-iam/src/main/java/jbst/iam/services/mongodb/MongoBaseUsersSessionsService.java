package jbst.iam.services.mongodb;

import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.mongodb.MongoUsersSessionsRepository;
import jbst.iam.services.abstracts.AbstractBaseUsersSessionsService;
import jbst.iam.utils.SecurityJwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jbst.foundation.utils.UserMetadataUtils;

@Service
public class MongoBaseUsersSessionsService extends AbstractBaseUsersSessionsService {

    @Autowired
    public MongoBaseUsersSessionsService(
            SecurityJwtEventsPublisher securityJwtEventsPublisher,
            MongoUsersSessionsRepository usersSessionsRepository,
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
