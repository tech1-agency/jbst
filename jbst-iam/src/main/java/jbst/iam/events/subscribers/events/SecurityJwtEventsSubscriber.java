package jbst.iam.events.subscribers.events;

import jbst.iam.domain.events.*;
import org.springframework.context.event.EventListener;

public interface SecurityJwtEventsSubscriber {
    @EventListener
    void onAuthenticationLogin(EventAuthenticationLogin event);
    @EventListener
    void onAuthenticationLoginFailure(EventAuthenticationLoginFailure event);
    @EventListener
    void onAuthenticationLogout(EventAuthenticationLogout event);
    @EventListener
    void onRegistration0(EventRegistration0 event);
    @EventListener
    void onRegistration0Failure(EventRegistration0Failure event);
    @EventListener
    void onRegistration1(EventRegistration1 event);
    @EventListener
    void onRegistration1Failure(EventRegistration1Failure event);
    @EventListener
    void onSessionRefreshed(EventSessionRefreshed event);
    @EventListener
    void onSessionExpired(EventSessionExpired event);
    @EventListener
    void onSessionUserRequestMetadataAdd(EventSessionUserRequestMetadataAdd event);
    @EventListener
    void onSessionUserRequestMetadataRenew(EventSessionUserRequestMetadataRenew event);
}
