package jbst.iam.domain.dto.requests;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import tech1.framework.foundation.domain.constants.ZoneIdsConstants;
import tech1.framework.foundation.domain.tests.runners.AbstractSerializationDeserializationRunner;

import static org.assertj.core.api.Assertions.assertThat;

class RequestUserUpdate2Test extends AbstractSerializationDeserializationRunner {
    private static final RequestUserUpdate2 REQUEST = new RequestUserUpdate2(
            ZoneIdsConstants.UKRAINE,
            "Tech1 Tests"
    );

    @Override
    protected String getFolder() {
        return "dto/requests";
    }

    @Override
    protected String getFileName() {
        return "user-update-2.json";
    }

    // serialization is not required for request-based dtos

    @SneakyThrows
    @Test
    void deserializeTest() {
        // Arrange
        var json = this.readFile();
        var typeReference = new TypeReference<RequestUserUpdate2>() {};

        // Act
        var actual = OBJECT_MAPPER.readValue(json, typeReference);

        // Assert
        assertThat(actual).isEqualTo(REQUEST);
    }
}
