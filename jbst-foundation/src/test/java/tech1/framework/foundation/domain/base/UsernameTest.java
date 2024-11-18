package tech1.framework.foundation.domain.base;

import com.fasterxml.jackson.core.type.TypeReference;
import tech1.framework.foundation.domain.tests.runners.AbstractSerializationDeserializationRunner;
import tech1.framework.foundation.domain.tests.constants.TestsJunitConstants;
import lombok.SneakyThrows;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsernameTest extends AbstractSerializationDeserializationRunner {
    private static final Username USERNAME = Username.testsHardcoded();

    @Override
    protected String getFileName() {
        return "username-1.json";
    }

    @Override
    protected String getFolder() {
        return "base";
    }

    @Test
    void serializeTest() {
        // Act
        var json = this.writeValueAsString(USERNAME);

        // Assert
        assertThat(json).isEqualTo(this.readFile());
    }

    @SneakyThrows
    @Test
    void deserializeTest() {
        // Arrange
        var json = this.readFile();
        var typeReference = new TypeReference<Username>() {};

        // Act
        var actual = OBJECT_MAPPER.readValue(json, typeReference);

        // Assert
        assertThat(actual).isEqualTo(USERNAME);
        assertThat(actual.value()).isEqualTo(USERNAME.value());
        assertThat(actual.toString()).hasToString(USERNAME.value());
    }

    @RepeatedTest(TestsJunitConstants.SMALL_ITERATIONS_COUNT)
    void randomTest() {
        // Act
        var actual = Username.random();

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.value()).isNotNull();
    }
}
