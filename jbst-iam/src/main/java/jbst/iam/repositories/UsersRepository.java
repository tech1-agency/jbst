package jbst.iam.repositories;

import jbst.iam.domain.db.Invitation;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.domain.identifiers.UserId;
import jbst.iam.domain.jwt.JwtUser;
import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.tuples.TuplePresence;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UsersRepository {
    TuplePresence<JwtUser> isPresent(UserId userId);
    JwtUser loadUserByUsername(Username username) throws UsernameNotFoundException;
    JwtUser findByUsernameAsJwtUserOrNull(Username username);
    JwtUser findByEmailAsJwtUserOrNull(Email email);
    long count();
    UserId saveAs(JwtUser user);
    UserId saveAs(RequestUserRegistration0 requestUserRegistration0, Password password);
    UserId saveAs(RequestUserRegistration1 requestUserRegistration1, Password password, Invitation invitation);
}
