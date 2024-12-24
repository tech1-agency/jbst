package jbst.foundation.domain.properties.utilties;

import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.hardware.monitoring.HardwareName;
import jbst.foundation.domain.properties.base.ScheduledJob;
import jbst.foundation.domain.properties.base.SchedulerConfiguration;
import jbst.foundation.domain.properties.base.SpringLogging;
import jbst.foundation.domain.properties.base.SpringServer;
import jbst.foundation.domain.properties.configs.*;
import jbst.foundation.domain.properties.configs.security.jwt.IncidentsConfigs;
import jbst.foundation.domain.tests.classes.NotUsedPropertiesConfigs;
import jbst.foundation.utilities.collections.CollectorUtility;
import jbst.foundation.utilities.enums.EnumUtility;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.util.Map.entry;
import static jbst.foundation.domain.properties.base.JbstIamIncidentType.*;
import static jbst.foundation.utilities.random.RandomUtility.randomBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;

class PropertiesAsserterAndPrinterTest {

    @Test
    void notUsedPropertiesConfigsTest() {
        // Arrange
        var notUsedPropertiesConfigs = new NotUsedPropertiesConfigs(
                new ScheduledJob(true, SchedulerConfiguration.hardcoded()),
                new SpringServer(8080),
                new SpringLogging("logback-test.xml")
        );

        // Act
        notUsedPropertiesConfigs.assertProperties(new PropertyId("notUsedPropertiesConfigs"));
        notUsedPropertiesConfigs.printProperties(new PropertyId("notUsedPropertiesConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void serverConfigsTest() {
        // Act
        ServerConfigs.hardcoded().assertProperties(new PropertyId("serverConfigs"));
        ServerConfigs.hardcoded().printProperties(new PropertyId("serverConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void utilitiesConfigsTest() {
        // Act
        UtilitiesConfigs.hardcoded().assertProperties(new PropertyId("utilitiesConfigs"));
        UtilitiesConfigs.hardcoded().printProperties(new PropertyId("utilitiesConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void asyncConfigsTest() {
        // Act
        AsyncConfigs.hardcoded().assertProperties(new PropertyId("asyncConfigs"));
        AsyncConfigs.hardcoded().printProperties(new PropertyId("asyncConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void eventsConfigsTest() {
        // Act
        EventsConfigs.hardcoded().assertProperties(new PropertyId("eventsConfigs"));
        EventsConfigs.hardcoded().printProperties(new PropertyId("eventsConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void mvcConfigsDisabledTest() {
        // Arrange
        var mvcConfigs = new MvcConfigs(false, null, null);

        // Act
        mvcConfigs.assertProperties(new PropertyId("mvcConfigs"));
        mvcConfigs.printProperties(new PropertyId("mvcConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void mvcConfigsTest() {
        // Act
        MvcConfigs.hardcoded().assertProperties(new PropertyId("mvcConfigs"));
        MvcConfigs.hardcoded().printProperties(new PropertyId("mvcConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void emailConfigsDisabledTest() {
        // Act
        EmailConfigs.disabled().assertProperties(new PropertyId("emailConfigs"));
        EmailConfigs.disabled().printProperties(new PropertyId("emailConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void emailConfigsTest() {
        // Act
        EmailConfigs.hardcoded().assertProperties(new PropertyId("emailConfigs"));
        EmailConfigs.hardcoded().printProperties(new PropertyId("emailConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void incidentConfigsTest() {
        // Act
        IncidentConfigs.hardcoded().assertProperties(new PropertyId("incidentConfigs"));
        IncidentConfigs.hardcoded().printProperties(new PropertyId("incidentConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void hardwareMonitoringConfigsDisabledTest() {
        // Arrange
        var hardwareMonitoringConfigs = HardwareMonitoringConfigs.disabled();

        // Act
        HardwareMonitoringConfigs.disabled().assertProperties(new PropertyId("hardwareMonitoringConfigs"));

        // Assert
        var thresholdsConfigs = hardwareMonitoringConfigs.getThresholdsConfigs();
        assertThat(thresholdsConfigs).hasSize(5);
        assertThat(thresholdsConfigs.keySet()).isEqualTo(EnumUtility.set(HardwareName.class));
        assertThat(thresholdsConfigs.values().stream().distinct().collect(CollectorUtility.toSingleton())).isEqualTo(ZERO);
    }

    @Test
    void hardwareMonitoringConfigsExceptionTest() {
        // Arrange
        var hardwareMonitoringConfigs = new HardwareMonitoringConfigs(
                true,
                Map.ofEntries(
                    entry(HardwareName.CPU, new BigDecimal("80")),
                    entry(HardwareName.HEAP, new BigDecimal("85"))
                )
        );

        // Act
        var throwable = catchThrowable(() -> hardwareMonitoringConfigs.assertProperties(new PropertyId("hardwareMonitoringConfigs")));

        // Assert
        assertThat(throwable).isNotNull();
        assertThat(throwable.getClass()).isEqualTo(IllegalArgumentException.class);
        assertThat(throwable.getMessage()).isEqualTo("Property \"\u001B[31mhardwareMonitoringConfigs.thresholdsConfigs\u001B[0m\" is invalid. Options: \"[CPU, HEAP, SERVER, SWAP, VIRTUAL]\". Required: \"[CPU, HEAP]\". Disjunction: \"[\u001B[31mSERVER, SWAP, VIRTUAL\u001B[0m]\"");
    }

    @Test
    void hardwareMonitoringConfigsTest() {
        // Act
        HardwareMonitoringConfigs.hardcoded().assertProperties(new PropertyId("hardwareMonitoringConfigs"));
        HardwareMonitoringConfigs.hardcoded().printProperties(new PropertyId("hardwareMonitoringConfigs"));

        // Assert
        var thresholdsConfigs = HardwareMonitoringConfigs.hardcoded().getThresholdsConfigs();
        assertThat(thresholdsConfigs).hasSize(5);
        assertThat(thresholdsConfigs.keySet()).isEqualTo(EnumUtility.set(HardwareName.class));
        assertThat(new HashSet<>(thresholdsConfigs.values())).hasSize(5);
    }

    @Test
    void hardwareServerConfigsTest() {
        // Act
        HardwareServerConfigs.hardcoded().assertProperties(new PropertyId("hardwareServerConfigs"));
        HardwareServerConfigs.hardcoded().printProperties(new PropertyId("hardwareServerConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void securityJwtConfigsDisabledUsersEmailsConfigsTest() {
        // Act
        var securityJwtConfigs = SecurityJwtConfigs.disabledUsersEmailsConfigs();

        // Act
        var throwable = catchThrowable(() -> securityJwtConfigs.assertProperties(new PropertyId("securityJwtConfigs")));

        // Assert
        assertThat(throwable).isNotNull();
        assertThat(throwable.getClass()).isEqualTo(IllegalArgumentException.class);
        assertThat(throwable.getMessage()).startsWith("Property");
        assertThat(throwable.getMessage()).endsWith(" is invalid");
    }

    @Test
    void securityJwtConfigsTest() {
        // Act
        SecurityJwtConfigs.hardcoded().assertProperties(new PropertyId("securityJwtConfigs"));
        SecurityJwtConfigs.hardcoded().printProperties(new PropertyId("securityJwtConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void securityJwtConfigsIncidentsCorrectTest() {
        var loginFailureUsernamePassword = randomBoolean();
        var loginFailureUsernameMaskedPassword = !loginFailureUsernamePassword;
        var incidentConfigs = new IncidentsConfigs(
                Map.ofEntries(
                        entry(AUTHENTICATION_LOGIN, randomBoolean()),
                        entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD, loginFailureUsernamePassword),
                        entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD, loginFailureUsernameMaskedPassword),
                        entry(AUTHENTICATION_LOGOUT, randomBoolean()),
                        entry(AUTHENTICATION_LOGOUT_MIN, randomBoolean()),
                        entry(SESSION_REFRESHED, randomBoolean()),
                        entry(SESSION_EXPIRED, randomBoolean()),
                        entry(REGISTER0, randomBoolean()),
                        entry(REGISTER0_FAILURE, randomBoolean()),
                        entry(REGISTER1, randomBoolean()),
                        entry(REGISTER1_FAILURE, randomBoolean())
                )
        );
        var securityJwtConfigs = new SecurityJwtConfigs(
                SecurityJwtConfigs.hardcoded().getAuthoritiesConfigs(),
                SecurityJwtConfigs.hardcoded().getCookiesConfigs(),
                SecurityJwtConfigs.hardcoded().getEssenceConfigs(),
                incidentConfigs,
                SecurityJwtConfigs.hardcoded().getJwtTokensConfigs(),
                SecurityJwtConfigs.hardcoded().getLoggingConfigs(),
                SecurityJwtConfigs.hardcoded().getSessionConfigs(),
                SecurityJwtConfigs.hardcoded().getUsersEmailsConfigs(),
                SecurityJwtConfigs.hardcoded().getWebsocketsConfigs(),
                SecurityJwtConfigs.hardcoded().getUsersTokensConfigs()
        );

        // Act
        securityJwtConfigs.assertProperties(new PropertyId("securityJwtConfigs"));
        securityJwtConfigs.printProperties(new PropertyId("securityJwtConfigs"));

        // Assert
        // no asserts
    }

    @Test
    void securityJwtConfigsIncidentsNoSessionRefreshedFailureTest() {
        var incidentConfigs = new IncidentsConfigs(
                Map.ofEntries(
                    entry(AUTHENTICATION_LOGIN, randomBoolean()),
                    entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD, false),
                    entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD, true),
                    entry(AUTHENTICATION_LOGOUT, randomBoolean()),
                    entry(AUTHENTICATION_LOGOUT_MIN, randomBoolean()),
                    entry(SESSION_EXPIRED, randomBoolean()),
                    entry(REGISTER0, randomBoolean()),
                    entry(REGISTER0_FAILURE, randomBoolean()),
                    entry(REGISTER1, randomBoolean()),
                    entry(REGISTER1_FAILURE, randomBoolean())
                )
        );
        var securityJwtConfigs = new SecurityJwtConfigs(
                SecurityJwtConfigs.hardcoded().getAuthoritiesConfigs(),
                SecurityJwtConfigs.hardcoded().getCookiesConfigs(),
                SecurityJwtConfigs.hardcoded().getEssenceConfigs(),
                incidentConfigs,
                SecurityJwtConfigs.hardcoded().getJwtTokensConfigs(),
                SecurityJwtConfigs.hardcoded().getLoggingConfigs(),
                SecurityJwtConfigs.hardcoded().getSessionConfigs(),
                SecurityJwtConfigs.hardcoded().getUsersEmailsConfigs(),
                SecurityJwtConfigs.hardcoded().getWebsocketsConfigs(),
                SecurityJwtConfigs.hardcoded().getUsersTokensConfigs()
        );

        // Act
        var throwable = catchThrowable(() -> securityJwtConfigs.assertProperties(new PropertyId("securityJwtConfigs")));

        // Assert
        assertThat(throwable).isNotNull();
        assertThat(throwable.getClass()).isEqualTo(IllegalArgumentException.class);
        assertThat(throwable.getMessage()).isEqualTo("Property \"\u001B[31msecurityJwtConfigs.incidentsConfigs.typesConfigs\u001B[0m\" is invalid. Options: \"[Authentication Login, Authentication Login Failure Username/Masked Password, Authentication Login Failure Username/Password, Authentication Logout, Authentication Logout Min, Register0, Register0 Failure, Register1, Register1 Failure, Session Expired, Session Refreshed]\". Required: \"[Authentication Login, Authentication Login Failure Username/Masked Password, Authentication Login Failure Username/Password, Authentication Logout, Authentication Logout Min, Register0, Register0 Failure, Register1, Register1 Failure, Session Expired]\". Disjunction: \"[\u001B[31mSession Refreshed\u001B[0m]\"");
    }

    @Test
    void securityJwtConfigsIncidentsOnlyOneLoginFailureTest() {
        var incidentConfigs = new IncidentsConfigs(
                Map.ofEntries(
                    entry(AUTHENTICATION_LOGIN, randomBoolean()),
                    entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_PASSWORD, true),
                    entry(AUTHENTICATION_LOGIN_FAILURE_USERNAME_MASKED_PASSWORD, true),
                    entry(AUTHENTICATION_LOGOUT, randomBoolean()),
                    entry(AUTHENTICATION_LOGOUT_MIN, randomBoolean()),
                    entry(SESSION_REFRESHED, randomBoolean()),
                    entry(SESSION_EXPIRED, randomBoolean()),
                    entry(REGISTER0, randomBoolean()),
                    entry(REGISTER0_FAILURE, randomBoolean()),
                    entry(REGISTER1, randomBoolean()),
                    entry(REGISTER1_FAILURE, randomBoolean())
                )
        );
        var securityJwtConfigs = new SecurityJwtConfigs(
                SecurityJwtConfigs.hardcoded().getAuthoritiesConfigs(),
                SecurityJwtConfigs.hardcoded().getCookiesConfigs(),
                SecurityJwtConfigs.hardcoded().getEssenceConfigs(),
                incidentConfigs,
                SecurityJwtConfigs.hardcoded().getJwtTokensConfigs(),
                SecurityJwtConfigs.hardcoded().getLoggingConfigs(),
                SecurityJwtConfigs.hardcoded().getSessionConfigs(),
                SecurityJwtConfigs.hardcoded().getUsersEmailsConfigs(),
                SecurityJwtConfigs.hardcoded().getWebsocketsConfigs(),
                SecurityJwtConfigs.hardcoded().getUsersTokensConfigs()
        );

        // Act
        var throwable = catchThrowable(() -> securityJwtConfigs.assertProperties(new PropertyId("securityJwtConfigs")));

        // Assert
        assertThat(throwable).isNotNull();
        assertThat(throwable.getClass()).isEqualTo(IllegalArgumentException.class);
        assertThat(throwable.getMessage()).isEqualTo("Please configure login failure incident feature. Only one feature type could be enabled");
    }

    @Test
    void mongodbSecurityJwtConfigsTest() {
        // Act
        MongodbSecurityJwtConfigs.hardcoded().assertProperties(new PropertyId("mongodbSecurityJwtConfigs"));
        MongodbSecurityJwtConfigs.hardcoded().printProperties(new PropertyId("mongodbSecurityJwtConfigs"));

        // Assert
        // no asserts
    }
}
