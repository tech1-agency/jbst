package jbst.iam.events.subscribers.base;

import jbst.foundation.domain.base.UsernamePasswordCredentials;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.pubsub.AbstractEventSubscriber;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLogin;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLoginFailureUsernameMaskedPassword;
import jbst.foundation.incidents.domain.authetication.IncidentAuthenticationLoginFailureUsernamePassword;
import jbst.foundation.incidents.domain.session.IncidentSessionRefreshed;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.foundation.utils.UserMetadataUtils;
import jbst.iam.domain.enums.AccountAccessMethod;
import jbst.iam.domain.events.*;
import jbst.iam.domain.functions.FunctionAccountAccessed;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.subscribers.SecurityJwtSubscriber;
import jbst.iam.services.BaseUsersSessionsService;
import jbst.iam.services.BaseUsersTokensService;
import jbst.iam.services.UsersEmailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Objects.isNull;
import static jbst.foundation.domain.constants.JbstConstants.Logs.USER_ACTION;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseSecurityJwtSubscriber extends AbstractEventSubscriber implements SecurityJwtSubscriber {

    // Publishers
    private final SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher;
    // Services
    private final BaseUsersTokensService baseUsersTokensService;
    private final UsersEmailsService usersEmailsService;
    private final BaseUsersSessionsService baseUsersSessionsService;
    // Utils
    private final UserMetadataUtils userMetadataUtils;
    // Incidents
    private final IncidentPublisher incidentPublisher;

    @Override
    public void onAuthenticationLogin(EventAuthenticationLogin event) {
        LOGGER.debug(USER_ACTION, event.username(), "[sub, events] login");
    }

    @Override
    public void onAuthenticationLoginFailure(EventAuthenticationLoginFailure event) {
        try {
            LOGGER.debug(USER_ACTION, event.username(), "[sub, events] login failure");
            var userRequestMetadata = this.userMetadataUtils.getUserRequestMetadataProcessed(
                    event.ipAddress(),
                    event.userAgentHeader()
            );
            this.securityJwtIncidentsPublisher.publishAuthenticationLoginFailureUsernamePassword(
                    new IncidentAuthenticationLoginFailureUsernamePassword(
                            new UsernamePasswordCredentials(
                                    event.username(),
                                    event.password()
                            ),
                            userRequestMetadata
                    )
            );
            this.securityJwtIncidentsPublisher.publishAuthenticationLoginFailureUsernameMaskedPassword(
                    new IncidentAuthenticationLoginFailureUsernameMaskedPassword(
                            UsernamePasswordCredentials.mask5(
                                    event.username(),
                                    event.password()
                            ),
                            userRequestMetadata
                    )
            );
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }

    @Override
    public void onAuthenticationLogout(EventAuthenticationLogout event) {
        LOGGER.debug(USER_ACTION, event.username(), "[sub, events] logout");
    }

    @Override
    public void onRegistration0(EventRegistration0 event) {
        try {
            LOGGER.debug(USER_ACTION, event.requestUserRegistration0().username(), "[sub, events] register0");
            var requestUserRegistration0 = event.requestUserRegistration0();
            var userToken = this.baseUsersTokensService.saveAs(requestUserRegistration0.asRequestUserEmailConfirmationToken());
            this.usersEmailsService.executeEmailConfirmation(userToken.asFunctionEmailConfirmation(requestUserRegistration0.email()));
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }

    @Override
    public void onRegistration0Failure(EventRegistration0Failure event) {
        LOGGER.debug(USER_ACTION, event.username(), "[sub, events] register0 failure");
    }

    @Override
    public void onRegistration1(EventRegistration1 event) {
        LOGGER.debug(USER_ACTION, event.requestUserRegistration1().username(), "[sub, events] register1");
    }

    @Override
    public void onRegistration1Failure(EventRegistration1Failure event) {
        LOGGER.debug(USER_ACTION, event.username(), "[sub, events] register1 failure");
    }

    @Override
    public void onSessionRefreshed(EventSessionRefreshed event) {
        LOGGER.debug(USER_ACTION, event.session().username(), "[sub, events] session refreshed");
    }

    @Override
    public void onSessionExpired(EventSessionExpired event) {
        LOGGER.debug(USER_ACTION, event.session().username(), "[sub, events] session expired");
    }

    @Override
    public void onSessionUserRequestMetadataAdd(EventSessionUserRequestMetadataAdd event) {
        try {
            LOGGER.debug(USER_ACTION, event.username(), "[sub, events] session user request metadata add");
            var session = this.baseUsersSessionsService.saveUserRequestMetadata(event);
            var metadata = session.metadata();
            this.processSessionUserRequestMetadataAddEmails(event, metadata);
            this.processSessionUserRequestMetadataAddIncidents(event, metadata);
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }

    @Override
    public void onSessionUserRequestMetadataRenew(EventSessionUserRequestMetadataRenew event) {
        try {
            LOGGER.debug(USER_ACTION, event.username(), "[sub, events] session user request metadata renew, sessionId: " + event.session().id());
            this.baseUsersSessionsService.saveUserRequestMetadata(event);
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }

    // =================================================================================================================
    // PRIVATE METHODS
    // =================================================================================================================
    private void processSessionUserRequestMetadataAddEmails(
            EventSessionUserRequestMetadataAdd event,
            UserRequestMetadata metadata
    ) {
        if (isNull(event.email())) {
            return;
        }
        if (event.isAuthenticationLoginEndpoint()) {
            this.usersEmailsService.executeAuthenticationLogin(
                    new FunctionAccountAccessed(
                            event.username(),
                            event.email(),
                            metadata,
                            AccountAccessMethod.USERNAME_PASSWORD
                    )
            );
        }
        if (event.isAuthenticationRefreshTokenEndpoint()) {
            this.usersEmailsService.executeSessionRefreshed(
                    new FunctionAccountAccessed(
                            event.username(),
                            event.email(),
                            metadata,
                            AccountAccessMethod.SECURITY_TOKEN
                    )
            );
        }
    }

    private void processSessionUserRequestMetadataAddIncidents(
            EventSessionUserRequestMetadataAdd event,
            UserRequestMetadata metadata
    ) {
        if (event.isAuthenticationLoginEndpoint()) {
            this.securityJwtIncidentsPublisher.publishAuthenticationLogin(
                    new IncidentAuthenticationLogin(
                            event.username(),
                            metadata
                    )
            );
        }
        if (event.isAuthenticationRefreshTokenEndpoint()) {
            this.securityJwtIncidentsPublisher.publishSessionRefreshed(
                    new IncidentSessionRefreshed(
                            event.username(),
                            metadata
                    )
            );
        }
    }
}
