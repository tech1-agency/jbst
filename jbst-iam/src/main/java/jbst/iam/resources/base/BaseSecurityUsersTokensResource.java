package jbst.iam.resources.base;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.concurrent.RateLimiter;
import jbst.foundation.domain.exceptions.authentication.JbstPasswordResetException;
import jbst.foundation.domain.exceptions.base.TooManyRequestsException;
import jbst.foundation.domain.exceptions.tokens.UserEmailConfirmException;
import jbst.foundation.domain.exceptions.tokens.UserTokenValidationException;
import jbst.foundation.domain.factories.concurrent.RateLimiterFactory;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.iam.annotations.AbstractJbstBaseSecurityResource;
import jbst.iam.assistants.current.CurrentSessionAssistant;
import jbst.iam.domain.dto.requests.RequestUserEmail;
import jbst.iam.domain.dto.requests.RequestUserPasswordReset;
import jbst.iam.domain.dto.requests.RequestUserToken;
import jbst.iam.services.BaseUsersService;
import jbst.iam.services.BaseUsersTokensService;
import jbst.iam.services.base.BaseUsersEmailsService;
import jbst.iam.validators.BaseUsersTokensRequestsValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

// Swagger
@Tag(name = "[jbst] Tokens API")
// Spring
@Slf4j
@AbstractJbstBaseSecurityResource
@RestController
@RequestMapping("/tokens")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseSecurityUsersTokensResource {

    // Assistants
    private final CurrentSessionAssistant currentSessionAssistant;
    // Services
    private final BaseUsersTokensService baseUsersTokensService;
    private final BaseUsersEmailsService baseUsersEmailsService;
    private final BaseUsersService baseUsersService;
    // Validators
    private final BaseUsersTokensRequestsValidator baseUsersTokensRequestsValidator;
    // Incidents
    private final IncidentPublisher incidentPublisher;
    // Properties
    private final JbstProperties jbstProperties;

    private final RateLimiter<Username> emailConfirmationRL = RateLimiterFactory.executeEmailConfirmation();

    @PostMapping("/email/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void executeConfirmEmail() throws TooManyRequestsException {
        var user = this.currentSessionAssistant.getCurrentJwtUser();
        this.baseUsersTokensRequestsValidator.validateExecuteConfirmEmail(user);
        this.emailConfirmationRL.acquire(user.username());
        var requestUserToken = RequestUserToken.emailConfirmation(user.username());
        var userToken = this.baseUsersTokensService.getOrCreate(requestUserToken);
        this.baseUsersEmailsService.executeEmailConfirmation(userToken.asFunctionEmailConfirmation(user.email()));
    }

    @ApiResponse(responseCode = "302", content = @Content(schema = @Schema(implementation = String.class)))
    @GetMapping("/email/confirm")
    public RedirectView confirmEmail(
            RedirectAttributes redirectAttributes,
            @RequestParam("token") String token
    ) {
        var redirectView = new RedirectView(this.jbstProperties.getEmailConfirmationRedirectURL());
        try {
            this.baseUsersTokensRequestsValidator.validateEmailConfirmationToken(token);
            this.baseUsersTokensService.confirmEmail(token);
            redirectAttributes.addAttribute("code", 1);
            return redirectView;
        } catch (UserTokenValidationException | UserEmailConfirmException ex) {
            redirectAttributes.addAttribute("code", 0);
            return redirectView;
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
            redirectAttributes.addAttribute("code", 0);
            return redirectView;
        }
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.OK)
    public void executeResetPassword(@RequestBody @Valid RequestUserEmail request) {
        try {
            var user = this.baseUsersService.findByEmail(request.email());
            this.baseUsersTokensRequestsValidator.validateExecuteResetPassword(user);
            var requestUserToken = RequestUserToken.passwordReset(user.username());
            var userToken = this.baseUsersTokensService.getOrCreate(requestUserToken);
            var functionResetPassword = userToken.asFunctionPasswordReset(user.email());
            this.baseUsersEmailsService.executePasswordReset(functionResetPassword);
        } catch (JbstPasswordResetException ex) {
            // ignored
        }
    }

    @PatchMapping("/password/reset")
    @ResponseStatus(HttpStatus.OK)
    public void resetPassword(@RequestBody @Valid RequestUserPasswordReset request) throws UserTokenValidationException {
        this.baseUsersTokensRequestsValidator.validatePasswordReset(request);
        this.baseUsersService.resetPassword(request);
    }

}
