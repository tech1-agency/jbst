package jbst.iam.domain.dto.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.tests.runners.AbstractSerializationDeserializationRunner;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static jbst.foundation.domain.constants.JbstConstants.ZoneIds.UKRAINE;
import static org.assertj.core.api.Assertions.assertThat;

class RequestUserRegistration1Test extends AbstractSerializationDeserializationRunner {
    private static final RequestUserRegistration1 REQUEST = new RequestUserRegistration1(
            Username.hardcoded(),
            Password.of("password123"),
            Password.of("password123"),
            UKRAINE,
            "TJ5veLJvqi78AARpiDVXQ9u0q9rbo3zpE6LtbWBH"
    );

    @Override
    protected String getFolder() {
        return "dto/requests";
    }

    @Override
    protected String getFileName() {
        return "user-registration-1.json";
    }

    // serialization is not required for request-based dtos

    @SneakyThrows
    @Test
    void deserializeTest() {
        // Arrange
        var json = this.readFile();
        var typeReference = new TypeReference<RequestUserRegistration1>() {};

        // Act
        var actual = OBJECT_MAPPER.readValue(json, typeReference);

        // Assert
        assertThat(actual).isEqualTo(REQUEST);
    }
}
