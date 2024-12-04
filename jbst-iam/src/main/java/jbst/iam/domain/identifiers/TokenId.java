package jbst.iam.domain.identifiers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jbst.foundation.domain.constants.JbstConstants;
import org.jetbrains.annotations.NotNull;

import static jbst.foundation.utilities.random.RandomUtility.randomString;

public record TokenId(@NotNull String value) {

    @JsonCreator
    public static TokenId of(String value) {
        return new TokenId(value);
    }

    public static TokenId hardcoded() {
        return of("75e0d0dfc0d34914a1c49305d6477abd");
    }

    public static TokenId random() {
        return new TokenId(randomString());
    }

    @SuppressWarnings("unused")
    public static TokenId unknown() {
        return of(JbstConstants.Strings.UNKNOWN);
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

}
