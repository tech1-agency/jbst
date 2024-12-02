package jbst.iam.services.abstracts;

import jbst.iam.domain.db.Invitation;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.repositories.InvitationsRepository;
import jbst.iam.repositories.UsersRepository;
import jbst.iam.services.BaseRegistrationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jbst.foundation.domain.base.Password;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractBaseRegistrationService implements BaseRegistrationService {

    // Repository
    private final InvitationsRepository invitationsRepository;
    private final UsersRepository usersRepository;
    // Password
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void register0(RequestUserRegistration0 requestUserRegistration0) {
        var hashPassword = this.bCryptPasswordEncoder.encode(requestUserRegistration0.password().value());
        this.usersRepository.saveAs(requestUserRegistration0, Password.of(hashPassword));
    }

    @Override
    public void register1(RequestUserRegistration1 requestUserRegistration1) {
        var invitation = this.invitationsRepository.findByCodeAsAny(requestUserRegistration1.code());
        var hashPassword = this.bCryptPasswordEncoder.encode(requestUserRegistration1.password().value());
        invitation = new Invitation(
                invitation.id(),
                invitation.owner(),
                invitation.authorities(),
                invitation.code(),
                requestUserRegistration1.username()
        );
        this.usersRepository.saveAs(requestUserRegistration1, Password.of(hashPassword), invitation);
        this.invitationsRepository.saveAs(invitation);
    }
}
