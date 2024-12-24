package jbst.iam.server.base.events.subscribers;

import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.foundation.utils.UserMetadataUtils;
import jbst.iam.domain.events.EventAuthenticationLogin;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.subscribers.events.base.BaseSecurityJwtEventsSubscriber;
import jbst.iam.services.BaseUsersSessionsService;
import jbst.iam.services.BaseUsersTokensService;
import jbst.iam.services.UsersEmailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityJwtEventsSubscriberImpl extends BaseSecurityJwtEventsSubscriber {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public SecurityJwtEventsSubscriberImpl(
            SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher,
            BaseUsersTokensService baseUsersTokensService,
            UsersEmailsService usersEmailsService,
            BaseUsersSessionsService baseUsersSessionsService,
            UserMetadataUtils userMetadataUtils,
            IncidentPublisher incidentPublisher
    ) {
        super(
                securityJwtIncidentsPublisher,
                baseUsersTokensService,
                usersEmailsService,
                baseUsersSessionsService,
                userMetadataUtils,
                incidentPublisher
        );
    }

    @Override
    public void onAuthenticationLogin(EventAuthenticationLogin event) {
        super.onAuthenticationLogin(event);
        LOGGER.info("[Server] SecurityJwtSubscriber.onAuthenticationLogin(). Username: {}", event.username());
    }
}
