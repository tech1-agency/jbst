package io.tech1.framework.foundation.domain.concurrent;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TimerTaskState {
    CREATED("Created"),
    OPERATIVE("Operative"),
    STOPPED("Stopped");

    private final String value;

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public TimerTaskPermissions getPermissions() {
        return new TimerTaskPermissions(
                CREATED.equals(this) || STOPPED.equals(this),
                this.isOperative()
        );
    }

    public boolean isOperative() {
        return OPERATIVE.equals(this);
    }
}
