package tech1.framework.foundation.domain.hardware.memories;

import com.fasterxml.jackson.core.type.TypeReference;
import tech1.framework.foundation.domain.tests.io.TestsIOUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalMemoryTest extends AbstractMemoriesTest {

    private static Stream<Arguments> serializeDeserializeTest() {
        return Stream.of(
                Arguments.of(new GlobalMemory(
                        1073741824L,
                        1973741824L,
                        1073741824L,
                        1773741824L,
                        1073741824L,
                        1673741824L
                ), "global-memory-1.json"),
                Arguments.of(GlobalMemory.zeroUsage(), "global-memory-2.json")
        );
    }

    @ParameterizedTest
    @MethodSource("serializeDeserializeTest")
    void serializeTest(GlobalMemory globalMemory, String fileName) {
        // Act
        var json = this.writeValueAsString(globalMemory);

        // Assert
        assertThat(json).isEqualTo(TestsIOUtils.readFile(this.getFolder(), fileName));
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("serializeDeserializeTest")
    void deserializeTest(GlobalMemory globalMemory, String fileName) {
        // Arrange
        var json = TestsIOUtils.readFile(this.getFolder(), fileName);
        var typeReference = new TypeReference<GlobalMemory>() {};

        // Act
        var actual = OBJECT_MAPPER.readValue(json, typeReference);

        // Assert
        assertThat(actual).isEqualTo(globalMemory);
    }
}
