package jbst.foundation.domain.properties.base;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SecurityJwtIncidentType {
    AUTHENTICATION_LOGIN("Authentication Login"),
    AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD("Authentication Login Failure Username/Password"),
    AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD("Authentication Login Failure Username/Masked Password"),
    AUTHENTICATION_LOGOUT("Authentication Logout"),
    AUTHENTICATION_LOGOUT_MIN("Authentication Logout Min"),
    SESSION_REFRESHED("Session Refreshed"),
    SESSION_EXPIRED("Session Expired"),
    REGISTER0("Register0"),
    REGISTER0_FAILURE("Register0 Failure"),
    REGISTER1("Register1"),
    REGISTER1_FAILURE("Register1 Failure");

    private final String value;

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
