package io.tech1.framework.domain.utilities.numbers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static io.tech1.framework.domain.utilities.numbers.RoundingUtility.*;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoundingUtilityTest {

    private static Stream<Arguments> scaleTest() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(5941306.04212988495091641), 3, BigDecimal.valueOf(5941306.042)),
                Arguments.of(BigDecimal.valueOf(5941306.04212988495091641), 4, BigDecimal.valueOf(5941306.0421)),
                Arguments.of(BigDecimal.valueOf(5941306.04212988495091641), 5, BigDecimal.valueOf(5941306.04213))
        );
    }

    private static Stream<Arguments> divideTest() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(10), BigDecimal.valueOf(3), 3, BigDecimal.valueOf(3.333)),
                Arguments.of(BigDecimal.valueOf(10), BigDecimal.valueOf(3), 4, BigDecimal.valueOf(3.3333)),
                Arguments.of(BigDecimal.valueOf(10), BigDecimal.valueOf(3), 5, BigDecimal.valueOf(3.33333))
        );
    }

    private static Stream<Arguments> divideOrZeroTest() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(10), BigDecimal.valueOf(3), 3, BigDecimal.valueOf(3.333)),
                Arguments.of(BigDecimal.valueOf(10), null, 4, BigDecimal.ZERO),
                Arguments.of(BigDecimal.valueOf(10), BigDecimal.ZERO, 5, BigDecimal.ZERO)
        );
    }

    private static Stream<Arguments> divideOrOneTest() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(10), BigDecimal.valueOf(3),  3, BigDecimal.valueOf(3.333)),
                Arguments.of(BigDecimal.valueOf(10), null, 4, BigDecimal.ONE),
                Arguments.of(BigDecimal.valueOf(10), BigDecimal.ZERO, 5, BigDecimal.ONE),
                Arguments.of(BigDecimal.valueOf(10), null, 5, BigDecimal.ONE)
        );
    }

    private static Stream<Arguments> formatTest() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(5941306.04212988495091641), 3, "5 941 306,042"),
                Arguments.of(BigDecimal.valueOf(5941306.04212988495091641), 4, "5 941 306,0421"),
                Arguments.of(BigDecimal.valueOf(5941306.04212988495091641), 5, "5 941 306,04213")
        );
    }

    @ParameterizedTest
    @MethodSource("scaleTest")
    void scaleTest(BigDecimal value, Integer scale, BigDecimal expected) {
        BigDecimal actual;
        if (scale == DEFAULT_SCALE) {
            // Act
            actual = scale(value);
        } else {
            // Act
            actual = scale(value, scale);
        }
        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("divideTest")
    void divideTest(BigDecimal divider, BigDecimal divisor, int scale, BigDecimal expected) {
        BigDecimal actual;
        if (scale == DEFAULT_SCALE) {
            // Act
            actual = divide(divider, divisor);
        } else {
            // Act
            actual = divide(divider, divisor, scale);
        }
        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("divideOrZeroTest")
    void divideOrZeroTest(BigDecimal divider, BigDecimal divisor, int scale, BigDecimal expected) {
        // Act
        var actual = divideOrZero(divider, divisor, scale);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("divideOrOneTest")
    void divideOrOneTest(BigDecimal divider, BigDecimal divisor, int scale, BigDecimal expected) {
        // Arrange
        if (isNull(divisor)) {
            divisor = mock(BigDecimal.class);
            when(divisor.compareTo(any(BigDecimal.class))).thenThrow(new RuntimeException());
        }

        // Act
        var actual = divideOrOne(divider, divisor, scale);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("formatTest")
    void formatTest(BigDecimal value, int scale, String expected) {
        String actual;
        if (scale == DEFAULT_SCALE) {
            // Act
            actual = format(value);

            // Assert
            var actual2 = format(value, scale);
            assertThat(actual2).isEqualTo(expected);
        } else {
            // Act
            actual = format(value, scale);
        }
        // Assert
        assertThat(actual).isEqualTo(expected);
    }
}
