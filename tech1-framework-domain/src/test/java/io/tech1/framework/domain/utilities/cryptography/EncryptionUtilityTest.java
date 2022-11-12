package io.tech1.framework.domain.utilities.cryptography;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.tech1.framework.domain.utilities.cryptography.EncryptionUtility.decryptAes128;
import static io.tech1.framework.domain.utilities.cryptography.EncryptionUtility.encryptAes128;
import static io.tech1.framework.domain.utilities.random.RandomUtility.randomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class EncryptionUtilityTest {

    // Sequence: Value, Encryption Init Vector, Encryption Key, Encrypted Value
    private static Stream<Arguments> encryptDecryptAes128() {
        return Stream.of(
                Arguments.of("value1", "%fTt2fz!4XHuHAmZ", "$fV5h7qB6DZPE77o", "/COFxlvc/F0RuCAzYKF0Cqf+N/JX1w=="),
                Arguments.of("value2", "Z7Xbqf$FxeCZRA4t", "eiXEax94L!4t&8Y#", "zQ4SuJVWcXYeP2FQSDqjdoj7E2f94Q=="),
                Arguments.of("value3", "Lb#vw!aQoJ9w52&e", "Zy&$5LT9$6%%9b&R", "6gBMUX8BFHEzi2nUvXP9SMs0zSlqwg==")
        );
    }

    @ParameterizedTest
    @MethodSource("encryptDecryptAes128")
    public void encryptAes128Test(String value, String encryptionInitVector, String encryptionKey, String encryptedValue) {
        // Act
        var actual = encryptAes128(value, encryptionInitVector, encryptionKey);

        // Assert
        assertThat(actual).isEqualTo(encryptedValue);
    }

    @Test
    public void encryptAes128ThrowExceptionTest() {
        // Arrange
        var value = "value";
        var encryptionInitVector = "";
        var encryptionKey = randomString();

        // Act
        var actual = catchThrowable(() -> encryptAes128(value, encryptionInitVector, encryptionKey));

        // Assert
        assertThat(actual).isInstanceOf(IllegalArgumentException.class);
        assertThat(actual).hasMessageStartingWith("Encryption Failure. Algorithm: AES, 128");
    }

    @ParameterizedTest
    @MethodSource("encryptDecryptAes128")
    public void decryptAes128Test(String value, String encryptionInitVector, String encryptionKey, String encryptedValue) {
        // Act
        var actual = decryptAes128(encryptedValue, encryptionInitVector, encryptionKey);

        // Assert
        assertThat(actual).isEqualTo(value);
    }

    @Test
    public void decryptAes128ThrowExceptionTest() {
        // Arrange
        var value = "value";
        var encryptionInitVector = "";
        var encryptionKey = randomString();

        // Act
        var actual = catchThrowable(() -> decryptAes128(value, encryptionInitVector, encryptionKey));

        // Assert
        assertThat(actual).isInstanceOf(IllegalArgumentException.class);
        assertThat(actual).hasMessageStartingWith("Decryption Failure. Algorithm: AES, 128");
    }
}
