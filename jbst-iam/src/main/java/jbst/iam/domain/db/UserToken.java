package jbst.iam.domain.db;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.time.TimeAmount;
import jbst.foundation.utilities.random.RandomUtility;
import jbst.iam.domain.enums.UserTokenType;
import jbst.iam.domain.functions.FunctionEmailConfirmation;
import jbst.iam.domain.functions.FunctionPasswordReset;
import jbst.iam.domain.identifiers.TokenId;

import java.time.temporal.ChronoUnit;

import static jbst.foundation.utilities.time.TimestampUtility.getFutureRange;
import static jbst.foundation.utilities.time.TimestampUtility.isPast;

public record UserToken(
        TokenId id,
        Username username,
        String value,
        UserTokenType type,
        long expiryTimestamp,
        boolean used
) {

    public static UserToken hardcoded() {
        return new UserToken(
                TokenId.hardcoded(),
                Username.hardcoded(),
                "V2orWAWX4xlvam9V7u5aUqpgriM6qd8qRsgGyqNw",
                UserTokenType.EMAIL_CONFIRMATION,
                getFutureRange(new TimeAmount(24, ChronoUnit.HOURS)).to(),
                false
        );
    }

    public static UserToken random() {
        return new UserToken(
                TokenId.random(),
                Username.random(),
                RandomUtility.randomString(),
                RandomUtility.randomEnum(UserTokenType.class),
                RandomUtility.randomLongGreaterThanZero(),
                RandomUtility.randomBoolean()
        );
    }

    public UserToken withUsed(boolean used) {
        return new UserToken(
                this.id,
                this.username,
                this.value,
                this.type,
                this.expiryTimestamp,
                used
        );
    }

    public boolean isExpired() {
        return isPast(this.expiryTimestamp);
    }

    public FunctionEmailConfirmation asFunctionEmailConfirmation(Email email) {
        return new FunctionEmailConfirmation(
                this.username,
                email,
                this.value
        );
    }

    public FunctionPasswordReset asFunctionPasswordReset(Email email) {
        return new FunctionPasswordReset(
                this.username,
                email,
                this.value
        );
    }
}
