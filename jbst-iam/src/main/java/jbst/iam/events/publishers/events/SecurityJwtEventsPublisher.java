package jbst.iam.events.publishers.events;

import jbst.iam.domain.events.*;

public interface SecurityJwtEventsPublisher {
    void publishAuthenticationLogin(EventAuthenticationLogin event);
    void publishAuthenticationLoginFailure(EventAuthenticationLoginFailure event);
    void publishAuthenticationLogout(EventAuthenticationLogout event);
    void publishRegistration0(EventRegistration0 event);
    void publishRegistration0Failure(EventRegistration0Failure event);
    void publishRegistration1(EventRegistration1 event);
    void publishRegistration1Failure(EventRegistration1Failure event);
    void publishSessionRefreshed(EventSessionRefreshed event);
    void publishSessionExpired(EventSessionExpired event);
    void publishSessionUserRequestMetadataAdd(EventSessionUserRequestMetadataAdd event);
    void publishSessionUserRequestMetadataRenew(EventSessionUserRequestMetadataRenew event);
}
