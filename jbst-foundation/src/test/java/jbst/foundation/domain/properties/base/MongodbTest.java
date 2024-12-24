package jbst.foundation.domain.properties.base;

import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MongodbTest {

    private static Stream<Arguments> noAuthenticationTest() {
        return Stream.of(
                Arguments.of("127.0.0.1", 27017, "jbst", null, null),
                Arguments.of("127.0.0.1", 27017, "jbst", Username.random(), null),
                Arguments.of("127.0.0.1", 27017, "jbst", null, Password.random())
        );
    }

    @ParameterizedTest
    @MethodSource("noAuthenticationTest")
    void noAuthenticationTest(String host, int port, String database, Username username, Password password) {
        // Arrange
        var mongodb = Mongodb.noSecurity(host, port, database);
        mongodb.setUsername(username);
        mongodb.setPassword(password);

        // Act
        var actual = mongodb.connectionString();

        // Assert
        assertThat(actual).isEqualTo("mongodb://127.0.0.1:27017/jbst");
    }

    @Test
    void authenticationPresentTest() {
        // Arrange
        var mongodb = new Mongodb("127.0.0.1", 27017, "jbst", Username.of("admin"), Password.of("Password123!"));

        // Act
        var actual = mongodb.connectionString();

        // Assert
        assertThat(actual).isEqualTo("mongodb://admin:Password123!@127.0.0.1:27017/jbst");
    }
}
