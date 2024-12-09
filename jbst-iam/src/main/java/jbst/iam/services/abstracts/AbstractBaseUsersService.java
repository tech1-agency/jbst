package jbst.iam.services.abstracts;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.iam.domain.dto.requests.RequestUserChangePasswordBasic;
import jbst.iam.domain.dto.requests.RequestUserResetPassword;
import jbst.iam.domain.dto.requests.RequestUserUpdate1;
import jbst.iam.domain.dto.requests.RequestUserUpdate2;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.repositories.UsersRepository;
import jbst.iam.repositories.UsersTokensRepository;
import jbst.iam.services.BaseUsersService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@AllArgsConstructor
public abstract class AbstractBaseUsersService implements BaseUsersService {

    // Repository
    private final UsersTokensRepository usersTokensRepository;
    private final UsersRepository usersRepository;
    // Password
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public JwtUser findByEmail(Email email) {
        return this.usersRepository.findByEmailAsJwtUserOrNull(email);
    }

    @Override
    public void updateUser1(JwtUser user, RequestUserUpdate1 request) {
        user = new JwtUser(
                user.id(),
                user.username(),
                user.password(),
                request.zoneId(),
                user.authorities(),
                request.email(),
                request.name(),
                user.passwordChangeRequired(),
                user.emailDetails(),
                user.attributes()
        );
        this.saveAndReauthenticate(user);
    }

    @Override
    public void updateUser2(JwtUser user, RequestUserUpdate2 request) {
        user = new JwtUser(
                user.id(),
                user.username(),
                user.password(),
                request.zoneId(),
                user.authorities(),
                user.email(),
                request.name(),
                user.passwordChangeRequired(),
                user.emailDetails(),
                user.attributes()
        );
        this.saveAndReauthenticate(user);
    }

    @Override
    public void changePasswordRequired(JwtUser user, RequestUserChangePasswordBasic request) {
        var hashPassword = this.bCryptPasswordEncoder.encode(request.newPassword().value());
        user = new JwtUser(
                user.id(),
                user.username(),
                Password.of(hashPassword),
                user.zoneId(),
                user.authorities(),
                user.email(),
                user.name(),
                false,
                user.emailDetails(),
                user.attributes()
        );
        this.saveAndReauthenticate(user);
    }

    @Override
    public void changePassword1(JwtUser user, RequestUserChangePasswordBasic request) {
        var hashPassword = this.bCryptPasswordEncoder.encode(request.newPassword().value());
        user = new JwtUser(
                user.id(),
                user.username(),
                Password.of(hashPassword),
                user.zoneId(),
                user.authorities(),
                user.email(),
                user.name(),
                user.passwordChangeRequired(),
                user.emailDetails(),
                user.attributes()
        );
        this.saveAndReauthenticate(user);
    }

    @Override
    public void resetPassword(RequestUserResetPassword request) {
        var userToken = this.usersTokensRepository.findByValueAsAny(request.token());
        var hashPassword = this.bCryptPasswordEncoder.encode(request.newPassword().value());
        this.usersRepository.resetPassword(userToken.username(), Password.of(hashPassword));
        userToken = userToken.withUsed(true);
        this.usersTokensRepository.saveAs(userToken);
    }

    // ================================================================================================================
    // PROTECTED METHODS
    // ================================================================================================================
    protected void saveAndReauthenticate(JwtUser jwtUser) {
        this.usersRepository.saveAs(jwtUser);
        var authentication = new UsernamePasswordAuthenticationToken(jwtUser, null, jwtUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
