package jbst.iam.services.mongodb;

import jbst.iam.repositories.mongodb.MongoUsersRepository;
import jbst.iam.repositories.mongodb.MongoUsersTokensRepository;
import jbst.iam.services.abstracts.AbstractBaseUsersTokensService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MongoBaseUsersTokensService extends AbstractBaseUsersTokensService {

    @Autowired
    public MongoBaseUsersTokensService(
            MongoUsersTokensRepository usersTokensRepository,
            MongoUsersRepository usersRepository
    ) {
        super(
                usersTokensRepository,
                usersRepository
        );
    }
}
