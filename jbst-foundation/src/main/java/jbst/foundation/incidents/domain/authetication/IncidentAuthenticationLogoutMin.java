package jbst.foundation.incidents.domain.authetication;

import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.properties.base.JbstIamIncidentType;
import jbst.foundation.incidents.domain.AbstractIncident;
import jbst.foundation.incidents.domain.Incident;

public record IncidentAuthenticationLogoutMin(
        Username username
) implements AbstractIncident {

    @Override
    public Incident getPlainIncident() {
        return new Incident(
                JbstIamIncidentType.AUTHENTICATION_LOGOUT_MIN,
                this.username
        );
    }
}
