package jbst.foundation.incidents.domain;

import lombok.experimental.UtilityClass;

@UtilityClass
public class IncidentAttributes {

    @SuppressWarnings("unused")
    @UtilityClass
    public static class Keys {
        public static final String TYPE = "incidentType";

        public static final String EMAIL = "email";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";

        public static final String TRIGGER = "_trigger";

        public static final String EXCEPTION = "exception";
        public static final String TRACE = "trace";
        public static final String MESSAGE = "message";
        public static final String METHOD = "method";
        public static final String PARAMS = "params";

        public static final String INVITATION_CODE = "invitationCode";
        public static final String INVITATION_OWNER = "invitationOwner";

        public static final String BROWSER = "browser";
        public static final String COUNTRY = "country";
        public static final String COUNTRY_CODE = "countryCode";
        public static final String COUNTRY_FLAG = "countryFlag";
        public static final String IP_ADDRESS = "ipAddress";
        public static final String WHAT = "what";
        public static final String WHEN = "when";
        public static final String WHERE = "where";
    }

    @UtilityClass
    public static class IncidentsTypes {
        public static final String THROWABLE = "Throwable";
    }
}
