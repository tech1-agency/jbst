package jbst.iam.services.postgres;

import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.repositories.postgres.PostgresInvitationsRepository;
import jbst.iam.repositories.postgres.PostgresUsersRepository;
import jbst.iam.services.abstracts.AbstractBaseRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PostgresBaseRegistrationService extends AbstractBaseRegistrationService {

    @Autowired
    public PostgresBaseRegistrationService(
            PostgresInvitationsRepository invitationsRepository,
            PostgresUsersRepository usersRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        super(
                invitationsRepository,
                usersRepository,
                bCryptPasswordEncoder
        );
    }

    @Transactional
    @Override
    public void register0(RequestUserRegistration0 requestUserRegistration0) {
        super.register0(requestUserRegistration0);
    }

    @Transactional
    @Override
    public void register1(RequestUserRegistration1 requestUserRegistration1) {
        super.register1(requestUserRegistration1);
    }
}
