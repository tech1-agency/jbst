package io.tech1.framework.domain.states.classic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.tech1.framework.domain.enums.EnumValue;
import lombok.AllArgsConstructor;

import java.util.Comparator;
import java.util.Set;

import static io.tech1.framework.domain.utilities.enums.EnumCreatorUtility.findEnumByValueIgnoreCaseOrThrow;

@AllArgsConstructor
public enum ClassicState implements EnumValue<String> {
    DISABLED("Disabled"),
    CREATED("Created"),
    STARTING("Starting"),
    ACTIVE("Active"),
    PAUSING("Pausing"),
    PAUSED("Paused"),
    STOPPING("Stopping"),
    TERMINATED("Terminated"),
    COMPLETING("Completing"),
    COMPLETED("Completed");

    public static final Comparator<ClassicState> ORDINAL_COMPARATOR = Comparator.comparing(ClassicState::ordinal);

    private final String value;

    @JsonCreator
    public static ClassicState find(String value) {
        return findEnumByValueIgnoreCaseOrThrow(ClassicState.class, value);
    }

    @JsonValue
    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public ClassicStatePermissions getPermissions() {
        return new ClassicStatePermissions(
                this.isDisabled(),
                Set.of(CREATED, TERMINATED).contains(this),
                Set.of(CREATED, ACTIVE, PAUSED, TERMINATED, COMPLETED).contains(this),
                this.isActive(),
                this.isActiveOrPaused()
        );
    }

    public boolean isDisabled() {
        return DISABLED.equals(this);
    }

    public boolean isCreated() {
        return CREATED.equals(this);
    }

    public boolean isStarting() {
        return STARTING.equals(this);
    }

    public boolean isActive() {
        return ACTIVE.equals(this);
    }

    public boolean isPaused() {
        return PAUSED.equals(this);
    }

    public boolean isStopping() {
        return STOPPING.equals(this);
    }

    public boolean isTerminated() {
        return TERMINATED.equals(this);
    }

    public boolean isCompleted() {
        return COMPLETED.equals(this);
    }

    public boolean isCreatedOrDisabled() {
        return this.isCreated() || this.isDisabled();
    }

    public boolean isActiveOrPaused() {
        return this.isActive() || this.isPaused();
    }

    public boolean isActiveOrCompleted() {
        return this.isActive() || this.isCompleted();
    }
}
