package jbst.foundation.domain.constants;

import com.diogonunes.jcolor.AnsiFormat;
import jbst.foundation.domain.enums.Status;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.diogonunes.jcolor.Attribute.*;

@UtilityClass
public class JbstConstants {

    public class BigDecimals {
        public static final BigDecimal MINUS_ONE = BigDecimal.valueOf(-1);
        public static final BigDecimal TWO = BigDecimal.valueOf(2L);
        public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    }

    @SuppressWarnings("unused")
    public class BigIntegers {
        public static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);
        public static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100);
    }

    @SuppressWarnings("unused")
    public class DateTimeFormatters {
        public static final DateTimeFormatter DTF10 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");
        public static final DateTimeFormatter DTF11 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        public static final DateTimeFormatter DTF12 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        public static final DateTimeFormatter DTF13 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        public static final DateTimeFormatter DTF20 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        public static final DateTimeFormatter DTF21 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        public static final DateTimeFormatter DTF22 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        public static final DateTimeFormatter DTF23 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public static final DateTimeFormatter DTF30 = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss.SSS");
        public static final DateTimeFormatter DTF31 = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        public static final DateTimeFormatter DTF32 = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        public static final DateTimeFormatter DTF33 = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        public static final DateTimeFormatter DTF41 = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        public static final DateTimeFormatter DTF42 = DateTimeFormatter.ofPattern("dd MMMM");

        public static final DateTimeFormatter DTF51 = DateTimeFormatter.ofPattern("HH:mm:ss");
        public static final DateTimeFormatter DTF52 = DateTimeFormatter.ofPattern("HH:mm");
    }

    public static class Domains {
        public static final String HARDCODED = "yyluchkiv.com";
    }

    @SuppressWarnings("unused")
    public class Dropdowns {
        public static final String ALL = "All";
    }

    @SuppressWarnings("unused")
    public static class JColor {
        public static final AnsiFormat BLACK_BOLD_TEXT = new AnsiFormat(BLACK_TEXT(), BOLD());
        public static final AnsiFormat BLUE_BOLD_TEXT = new AnsiFormat(BLUE_TEXT(), BOLD());
        public static final AnsiFormat GREEN_BOLD_TEXT = new AnsiFormat(GREEN_TEXT(), BOLD());
        public static final AnsiFormat RED_BOLD_TEXT = new AnsiFormat(RED_TEXT(), BOLD());
        public static final AnsiFormat YELLOW_BOLD_TEXT = new AnsiFormat(YELLOW_TEXT(), BOLD());
    }

    public class Files {
        public static final String PATH_DELIMITER = "/";
    }

    @SuppressWarnings("unused")
    public class Logs {
        // =================================================================================================================
        // Prefixes
        // =================================================================================================================
        public static final String PREFIX = "[jbst]";
        public static final String PREFIX_OPEN = "[jbst, ";
        public static final String PREFIX_PROPERTIES = PREFIX_OPEN + "properties]";
        public static final String PREFIX_UTILITIES = PREFIX_OPEN + "utilities]";
        public static final String PREFIX_EVENTS = PREFIX_OPEN + "events]";
        public static final String PREFIX_INCIDENTS = PREFIX_OPEN + "incidents]";

        // =================================================================================================================
        // Events
        // =================================================================================================================
        public static final String EVENTS_AUTHENTICATION_LOGIN = PREFIX_EVENTS + " `{}` - /login. Username: `{}`";
        public static final String EVENTS_AUTHENTICATION_LOGIN_FAILURE = PREFIX_EVENTS + " `{}` - login failure. Username: `{}`";
        public static final String EVENTS_AUTHENTICATION_LOGOUT = PREFIX_EVENTS + " `{}`- /logout. Username: `{}`";
        public static final String EVENTS_REGISTER1 = PREFIX_EVENTS + " `{}`- /register1. Username: `{}`";
        public static final String EVENTS_REGISTER1_FAILURE = PREFIX_EVENTS + " `{}`- /register1 failure. Username: `{}`";
        public static final String EVENTS_SESSION_REFRESHED = PREFIX_EVENTS + " `{}`- /refreshToken. Username: `{}`";
        public static final String EVENTS_SESSION_EXPIRED = PREFIX_EVENTS + " `{}`- session expired. Username: `{}`";
        public static final String EVENTS_SESSION_ADD_USER_REQUEST_METADATA = PREFIX_EVENTS + " `{}`- Session add user request metadata. Username: `{}`";
        public static final String EVENTS_SESSION_RENEW_USER_REQUEST_METADATA = PREFIX_EVENTS + " `{}`- Session renew user request metadata. Username: `{}`. Session: `{}`";


        // =================================================================================================================
        // Incidents
        // =================================================================================================================
        public static final String INCIDENT_FEATURE_DISABLED = PREFIX_INCIDENTS + " `{}` feature is disabled";
        public static final String INCIDENT = PREFIX_INCIDENTS + " `{}`. incident type: `{}`";
        public static final String INCIDENT_AUTHENTICATION_LOGIN = PREFIX_INCIDENTS + " `{}` - /login. Username: `{}`";
        public static final String INCIDENT_AUTHENTICATION_LOGIN_FAILURE = PREFIX_INCIDENTS + " `{}` - /login failure. Username: `{}`";
        public static final String INCIDENT_AUTHENTICATION_LOGOUT = PREFIX_INCIDENTS + " `{}` - :/logout. Username: `{}`";
        public static final String INCIDENT_REGISTER1 = PREFIX_INCIDENTS + " `{}` - /register1. Username: `{}`";
        public static final String INCIDENT_REGISTER1_FAILURE = PREFIX_INCIDENTS + " `{}` - /register1 failure. Username: `{}`";
        public static final String INCIDENT_SESSION_REFRESHED = PREFIX_INCIDENTS + " `{}` - /refreshToken. Username: `{}`";
        public static final String INCIDENT_SESSION_EXPIRED = PREFIX_INCIDENTS + " `{}` - session expired. Username: `{}`";
        public static final String INCIDENT_SYSTEM_RESET_SERVER = PREFIX_INCIDENTS + " `{}` - system reset server. Username: `{}`. Status: `{}`";

        // =================================================================================================================
        // Tasks
        // =================================================================================================================
        public static final String TASK_RESET_SERVER = PREFIX_OPEN + "tasks] Reset Server Initiator: `{}`. Status: `{}`";

        // =================================================================================================================
        // Server
        // =================================================================================================================
        public static final String SERVER_OFFLINE = PREFIX + " `{}` is probably offline. Exception: `{}`";

        public static String getServerContainer(Status status) {
            return PREFIX + " " + JColor.BLACK_BOLD_TEXT.format("{}") + " container configuration. Status: " + status.formatAnsi();
        }

        public static String getServerStartup(Status status) {
            return PREFIX + " " + JColor.BLACK_BOLD_TEXT.format("{}") + " startup listener configuration. Status: " + status.formatAnsi();
        }
    }

    public static class MemoryUnits {
        public static final long BYTES_IN_KILOBYTE = 1024L;
        public static final long BYTES_IN_MEGABYTE = 1048576L;
        public static final long BYTES_IN_GIGABYTE = 1073741824L;
    }

    @SuppressWarnings("unused")
    public static class Properties {
        public static final String ACCOUNT = "account";
        public static final String ANY_PROBLEM = "anyProblem";
        public static final String DETAILS = "details";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String NOTIFICATION = "notification";
        public static final String NOTIFICATION_ONLY = "notificationOnly";
        public static final String PASSWORD = "password";
        public static final String STATS = "stats";
        public static final String STATUS = "status";
        public static final String STATUSES = "statuses";
        public static final String TIMESTAMP = "timestamp";
        public static final String USERNAME = "username";
        public static final String VALUE = "value";
        public static final String WEBSOCKET = "websocket";
    }

    @SuppressWarnings("unused")
    public static class Strings {
        public static final String ACCOUNT = "ACCOUNT";
        public static final String SUM = "SUM";
        public static final String TOTAL = "TOTAL";

        public static final String UNKNOWN = "Unknown";
        public static final String UNDEFINED = "[?]";

        public static final String OPS = "[Ops]";
    }

    public static class Swagger {
        public static final List<String> ENDPOINTS = List.of(
                "/v3/api-docs/**",
                "/swagger-ui.html",
                "/swagger-ui/**"
        );
    }

    @SuppressWarnings("unused")
    public static class Symbols {
        public static final String LINE_SEPARATOR_INTERPUNCT = "··································································································";

        public static final String COMMA = ",";
        public static final String DASH = "—";
        public static final String EMPTY = "";
        public static final String HYPHEN = "-";
        public static final String SEMICOLON = ";";
        public static final String SLASH = "/";

        public static final String TAB = "\t";
        public static final String NEWLINE = "\n";
        public static final String TWO_NEWLINE = "\n\n";

        public static final String COLLECTORS_COMMA_SPACE = ", ";
    }

    public static class ZoneIds {
        // Poland
        public static final ZoneId POLAND = ZoneId.of("Poland");

        // Ukraine, Kyiv
        // Daylight Saving Time; EEST: Eastern European Summer Time; UTC+3
        // Standard Time; EET: Eastern European Time; UTC+2
        // WARNING: https://github.com/eggert/tz/commit/e13e9c531fc48a04fb8d064acccc9f8ae68d5544
        public static final ZoneId UKRAINE = ZoneId.of("Europe/Kyiv");
    }
}
