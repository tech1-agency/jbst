package jbst.foundation.domain.triggers;

import com.fasterxml.jackson.annotation.JsonValue;
import jbst.foundation.domain.base.Username;

public record AutoTrigger(Username username) implements AbstractTrigger {

    @Override
    public Username getUsername() {
        return this.username;
    }

    @Override
    public TriggerType getTriggerType() {
        return TriggerType.AUTO;
    }

    @JsonValue
    @Override
    public String getReadableDetails() {
        return this.getTriggerType().getValue() + " trigger, username: " + this.getUsername();
    }
}
