package jbst.foundation.domain.exceptions;

import com.fasterxml.jackson.core.type.TypeReference;
import jbst.foundation.domain.tests.runners.AbstractSerializationDeserializationRunner;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowableTraceTest extends AbstractSerializationDeserializationRunner {
    private static final ThrowableTrace TRACE = new ThrowableTrace("java.lang.NullPointerException: jbst at jbst.domain.exceptions.ThrowableTraceTest.main(ThrowableTraceTest.java:20)");

    @Override
    protected String getFolder() {
        return "exceptions";
    }

    @Override
    protected String getFileName() {
        return "trace-1.json";
    }

    @Test
    void serializeTest() {
        // Act
        var json = this.writeValueAsString(TRACE);

        // Assert
        assertThat(json).isEqualTo(this.readFile());
    }

    @SneakyThrows
    @Test
    void deserializeTest() {
        // Arrange
        var json = this.readFile();
        var typeReference = new TypeReference<ThrowableTrace>() {};

        // Act
        var actual = OBJECT_MAPPER.readValue(json, typeReference);

        // Assert
        assertThat(actual).isEqualTo(TRACE);
        assertThat(actual.value()).isEqualTo(TRACE.value());
        assertThat(actual.toString()).hasToString(TRACE.value());
    }
}
