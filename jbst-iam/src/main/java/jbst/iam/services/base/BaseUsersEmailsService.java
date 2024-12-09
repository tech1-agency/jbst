package jbst.iam.services.base;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.base.Checkbox;
import jbst.foundation.domain.tuples.Tuple3;
import jbst.foundation.services.emails.domain.EmailHTML;
import jbst.foundation.services.emails.services.EmailService;
import jbst.iam.domain.enums.AccountAccessMethod;
import jbst.iam.domain.functions.FunctionAuthenticationLoginEmail;
import jbst.iam.domain.functions.FunctionEmailConfirmation;
import jbst.iam.domain.functions.FunctionPasswordReset;
import jbst.iam.domain.functions.FunctionSessionRefreshedEmail;
import jbst.iam.services.UsersEmailsService;
import jbst.iam.utils.UserEmailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseUsersEmailsService implements UsersEmailsService {

    // Services
    private final EmailService emailService;
    // Utilities
    private final UserEmailUtils userEmailUtils;
    // Properties
    private final JbstProperties jbstProperties;

    @Override
    public void executeEmailConfirmation(FunctionEmailConfirmation function) {
        var emailHTML = this.userEmailUtils.getEmailConfirmationHTML(function);
        this.emailService.sendHTML(emailHTML);
    }

    @Override
    public void executePasswordReset(FunctionPasswordReset function) {
        var emailHTML = this.userEmailUtils.getPasswordResetHTML(function);
        this.emailService.sendHTML(emailHTML);
    }

    @Override
    public void executeAuthenticationLogin(FunctionAuthenticationLoginEmail function) {
        this.executeBy(
                function.getTuple3(),
                this.jbstProperties.getSecurityJwtConfigs().getUsersEmailsConfigs().getAuthenticationLogin(),
                this.userEmailUtils.getAuthenticationLoginTemplateName(),
                AccountAccessMethod.USERNAME_PASSWORD
        );
    }

    @Override
    public void executeSessionRefreshed(FunctionSessionRefreshedEmail function) {
        this.executeBy(
                function.getTuple3(),
                this.jbstProperties.getSecurityJwtConfigs().getUsersEmailsConfigs().getSessionRefreshed(),
                this.userEmailUtils.getSessionRefreshedTemplateName(),
                AccountAccessMethod.SECURITY_TOKEN
        );
    }

    // =================================================================================================================
    // PRIVATE METHODS
    // =================================================================================================================
    private void executeBy(
            Tuple3<Username, Email, UserRequestMetadata> tuple3,
            Checkbox checkbox,
            String templateName,
            AccountAccessMethod accountAccessMethod
    ) {
        var email = tuple3.b();
        if (nonNull(email) && checkbox.isEnabled()) {
            this.emailService.sendHTML(
                    new EmailHTML(
                            Set.of(email.value()),
                            this.userEmailUtils.getSubject("Account Accessed"),
                            templateName,
                            this.userEmailUtils.getAuthenticationLoginOrSessionRefreshedVariables(
                                    tuple3.a(),
                                    tuple3.c(),
                                    accountAccessMethod
                            )
                    )
            );
        }
    }
}
