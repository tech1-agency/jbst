package jbst.iam.domain.identifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jbst.foundation.domain.constants.JbstConstants;
import org.jetbrains.annotations.NotNull;

import static jbst.foundation.utilities.random.RandomUtility.randomString;

public record InvitationId(@NotNull String value) {

    @JsonCreator
    public static InvitationId of(String value) {
        return new InvitationId(value);
    }

    public static InvitationId hardcoded() {
        return of("5EFCB2583361E1C7071E");
    }

    public static InvitationId random() {
        return new InvitationId(randomString());
    }

    @SuppressWarnings("unused")
    public static InvitationId unknown() {
        return of(JbstConstants.Strings.UNKNOWN);
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
