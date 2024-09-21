package io.tech1.framework.foundation.domain.tuples;

import io.tech1.framework.foundation.domain.tests.runners.AbstractFolderSerializationRunner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static io.tech1.framework.foundation.domain.tests.io.TestsIOUtils.readFile;
import static org.assertj.core.api.Assertions.assertThat;

class TupleReadableAmountTest extends AbstractFolderSerializationRunner {

    private static Stream<Arguments> serializationTest() {
        return Stream.of(
                Arguments.of(TupleReadableAmount.zero(3), "tuple-readable-amount-1.json"),
                Arguments.of(TupleReadableAmount.testsHardcoded(), "tuple-readable-amount-2.json"),
                Arguments.of(
                        new TupleReadableAmount(new BigDecimal("0.005"), false, true),
                        "tuple-readable-amount-3.json"
                ),
                Arguments.of(
                        new TupleReadableAmount(new BigDecimal("0.005"), true, false),
                        "tuple-readable-amount-4.json"
                ),
                Arguments.of(
                        new TupleReadableAmount(new BigDecimal("0.142857142857143"), true, true, 9),
                        "tuple-readable-amount-5.json"
                ),
                Arguments.of(
                        new TupleReadableAmount(new BigDecimal("112356.142857142"), true, true, 5),
                        "tuple-readable-amount-6.json"
                ),
                Arguments.of(
                        new TupleReadableAmount(new BigDecimal("0.0005"), 1),
                        "tuple-readable-amount-7.json"
                )
        );
    }

    @Override
    protected String getFolder() {
        return "tuples";
    }

    @ParameterizedTest
    @MethodSource("serializationTest")
    void serializationTest(TupleReadableAmount request, String fileName) {
        // Act
        var actual = this.writeValueAsString(request);

        // Assert
        assertThat(actual).isEqualTo(readFile(getFolder(), fileName));
    }
}
