package jbst.iam.domain.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;

import java.time.ZoneId;

import static jbst.foundation.utilities.random.RandomUtility.randomZoneId;

public record RequestUserRegistration0(
        @Email.ValidEmail Email email,
        @Username.ValidUsername Username username,
        @Password.ValidPasswordCamelCaseLettersAndNumbers(min = 8, max = 20) Password password,
        @Password.ValidPasswordNotBlank Password confirmPassword,
        @Schema(type = "string") @NotNull ZoneId zoneId
) {

    public static RequestUserRegistration0 hardcoded() {
        return new RequestUserRegistration0(
                Email.hardcoded(),
                Username.of("registration01"),
                Password.hardcoded(),
                Password.hardcoded(),
                randomZoneId()
        );
    }

    public void assertPasswordsOrThrow() {
        this.password.assertEqualsOrThrow(this.confirmPassword);
    }

}
