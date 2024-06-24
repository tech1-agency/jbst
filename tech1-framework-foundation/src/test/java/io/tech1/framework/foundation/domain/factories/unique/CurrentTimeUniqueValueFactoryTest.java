package io.tech1.framework.foundation.domain.factories.unique;

import io.tech1.framework.foundation.domain.tests.constants.TestsJunitConstants;
import io.tech1.framework.foundation.utilities.concurrent.SleepUtility;
import io.tech1.framework.foundation.utilities.time.TimestampUtility;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentTimeUniqueValueFactoryTest {

    private static Stream<Arguments> createValueCheckUniqueArgs() {
        return Stream.of(
                Arguments.of(100),
                Arguments.of(1000),
                Arguments.of(10000),
                Arguments.of(100000),
                Arguments.of(1000000)
        );
    }

    @RepeatedTest(TestsJunitConstants.RANDOM_ITERATIONS_COUNT)
    void createValueBetween() {
        // Arrange
        var window = 1;
        var randomFactory = new CurrentTimeUniqueValueFactory();

        // Act
        var value = randomFactory.createValue();

        // Assert
        assertThat(value)
                .isNotNull()
                .isBetween(
                        (int) (TimestampUtility.getCurrentTimestamp() / 1000) - window,
                        (int) (TimestampUtility.getCurrentTimestamp() / 1000) + window
                );
    }

    @ParameterizedTest
    @MethodSource("createValueCheckUniqueArgs")
    void createValueCheckUnique(int iterations) {
        // Arrange
        var randomFactory = new CurrentTimeUniqueValueFactory();

        // Act
        var values1 = IntStream.range(0, iterations).mapToObj(i -> randomFactory.createValue()).collect(Collectors.toSet());
        SleepUtility.sleep(TimeUnit.SECONDS, 2);
        var values2 = IntStream.range(0, iterations).mapToObj(i -> randomFactory.createValue()).collect(Collectors.toSet());

        // Assert
        assertThat(values1).hasSize(iterations);
        assertThat(values2).hasSize(iterations);
    }
}
