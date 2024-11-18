package jbst.foundation.incidents.domain.authetication;

import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.base.UsernamePasswordCredentials;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static jbst.foundation.domain.tests.constants.TestsFlagsConstants.UKRAINE;
import static org.assertj.core.api.Assertions.assertThat;

class IncidentAuthenticationLoginFailureUsernameMaskedPasswordTest {

    private static Stream<Arguments> ofTest() {
        return Stream.of(
                Arguments.of("123456789", "12345****"),
                Arguments.of("12345", "12345"),
                Arguments.of("abcdefghij", "abcde*****"),
                Arguments.of("abc", "abc")
        );
    }

    @ParameterizedTest
    @MethodSource("ofTest")
    void ofTest(String password, String expected) {
        // Arrange
        var username = Username.random();


        // Act
        var actual = new IncidentAuthenticationLoginFailureUsernameMaskedPassword(
                UsernamePasswordCredentials.mask5(
                        username,
                        new Password(password)
                ),
                UserRequestMetadata.valid()
        );

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.credentials().username()).isEqualTo(username);
        assertThat(actual.credentials().password().value()).isEqualTo(expected);
    }

    @Test
    void convertAuthenticationLoginFailureUsernameMaskedPasswordIncidentTest() {
        // Arrange
        var incident = new IncidentAuthenticationLoginFailureUsernameMaskedPassword(
                UsernamePasswordCredentials.mask5(
                        Username.testsHardcoded(),
                        Password.testsHardcoded()
                ),
                UserRequestMetadata.valid()
        );

        // Act
        var actual = incident.getPlainIncident();

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.getType()).isEqualTo("Authentication Login Failure Username/Masked Password");
        assertThat(actual.getUsername().value()).isEqualTo("tech1");
        assertThat(actual.getAttributes())
                .hasSize(8)
                .containsOnlyKeys("incidentType", "username", "password", "browser", "countryFlag", "ipAddress", "what", "where")
                .containsEntry("incidentType", "Authentication Login Failure Username/Masked Password")
                .containsEntry("username", incident.credentials().username())
                .containsEntry("password", incident.credentials().password())
                .containsEntry("browser", "Chrome")
                .containsEntry("countryFlag", UKRAINE)
                .containsEntry("ipAddress", "127.0.0.1")
                .containsEntry("what", "Chrome, macOS on Desktop")
                .containsEntry("where", "🇺🇦 Ukraine, Lviv");
    }
}
