package jbst.iam.events.subscribers.incidents.base;

import jbst.foundation.domain.pubsub.AbstractEventSubscriber;
import jbst.foundation.incidents.domain.authetication.*;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0Failure;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1Failure;
import jbst.foundation.incidents.domain.session.IncidentSessionExpired;
import jbst.foundation.incidents.domain.session.IncidentSessionRefreshed;
import jbst.foundation.incidents.feigns.clients.IncidentClient;
import jbst.iam.events.subscribers.incidents.SecurityJwtIncidentsSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static jbst.foundation.domain.constants.JbstConstants.Logs.*;

@SuppressWarnings("LoggingSimilarMessage")
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseSecurityJwtIncidentsSubscriber extends AbstractEventSubscriber implements SecurityJwtIncidentsSubscriber {

    // Clients
    private final IncidentClient incidentClient;

    @Override
    public void onEvent(IncidentAuthenticationLogin incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] login");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentAuthenticationLoginFailureUsernamePassword incident) {
        LOGGER.debug(USER_ACTION, incident.credentials().username(), "[sub, incidents] login failure");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentAuthenticationLoginFailureUsernameMaskedPassword incident) {
        LOGGER.debug(USER_ACTION, incident.credentials().username(), "[sub, incidents] login failure");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentAuthenticationLogoutMin incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] logout");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentAuthenticationLogoutFull incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] logout");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentRegistration0 incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] register0");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentRegistration0Failure incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] register0 failure");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentRegistration1 incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] register1");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentRegistration1Failure incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] register1 failure");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentSessionRefreshed incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[sub, incidents] session refreshed");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }

    @Override
    public void onEvent(IncidentSessionExpired incident) {
        LOGGER.debug(USER_ACTION, incident.username(), "[pub, incidents] session expired");
        this.incidentClient.registerIncident(incident.getPlainIncident());
    }
}
