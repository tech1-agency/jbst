package jbst.iam.validators;

import jbst.foundation.domain.exceptions.authentication.JbstPasswordResetException;
import jbst.foundation.domain.exceptions.tokens.UserTokenValidationException;
import jbst.iam.domain.dto.requests.RequestUserPasswordReset;
import jbst.iam.domain.jwt.JwtUser;

public interface BaseUsersTokensRequestsValidator {
    void validateExecuteConfirmEmail(JwtUser user);
    void validateEmailConfirmationToken(String token) throws UserTokenValidationException;
    void validateExecuteResetPassword(JwtUser user) throws JbstPasswordResetException;
    void validatePasswordReset(RequestUserPasswordReset request) throws UserTokenValidationException;
}
