package jbst.iam.events.publishers.incidents.impl;

import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.pubsub.AbstractEventPublisher;
import jbst.foundation.incidents.domain.authetication.*;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0Failure;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1Failure;
import jbst.foundation.incidents.domain.session.IncidentSessionExpired;
import jbst.foundation.incidents.domain.session.IncidentSessionRefreshed;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static jbst.foundation.domain.constants.JbstConstants.Logs.USER_ACTION;
import static jbst.foundation.domain.properties.base.JbstIamIncidentType.*;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseSecurityJwtIncidentsPublisher extends AbstractEventPublisher implements SecurityJwtIncidentsPublisher {

    // Spring Publisher
    private final ApplicationEventPublisher applicationEventPublisher;
    // Properties
    private final JbstProperties jbstProperties;

    @Override
    public void publishAuthenticationLogin(IncidentAuthenticationLogin incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(AUTHENTICATION_LOGIN)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] login");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishAuthenticationLoginFailureUsernamePassword(IncidentAuthenticationLoginFailureUsernamePassword incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD)) {
            LOGGER.debug(USER_ACTION, incident.credentials().username(), "[pub, incidents] login failure");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishAuthenticationLoginFailureUsernameMaskedPassword(IncidentAuthenticationLoginFailureUsernameMaskedPassword incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD)) {
            LOGGER.debug(USER_ACTION, incident.credentials().username(), "[pub, incidents] login failure");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishAuthenticationLogoutMin(IncidentAuthenticationLogoutMin incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(AUTHENTICATION_LOGOUT_MIN)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] logout");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishAuthenticationLogoutFull(IncidentAuthenticationLogoutFull incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(AUTHENTICATION_LOGOUT)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] logout");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishRegistration0(IncidentRegistration0 incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(REGISTER0)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] register0");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishRegistration0Failure(IncidentRegistration0Failure incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(REGISTER0_FAILURE)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] register0 failure");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishRegistration1(IncidentRegistration1 incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(REGISTER1)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] register1");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishRegistration1Failure(IncidentRegistration1Failure incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(REGISTER1_FAILURE)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] register1 failure");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishSessionRefreshed(IncidentSessionRefreshed incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(SESSION_REFRESHED)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] session refreshed");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }

    @Override
    public void publishSessionExpired(IncidentSessionExpired incident) {
        if (this.jbstProperties.getSecurityJwtConfigs().getIncidentsConfigs().isEnabled(SESSION_EXPIRED)) {
            LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] session expired");
            this.applicationEventPublisher.publishEvent(incident);
        }
    }
}
