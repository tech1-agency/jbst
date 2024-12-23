package jbst.foundation.domain.hardware.monitoring;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HardwareMonitoringDatapointTableViewTest {

    private static Stream<Arguments> constructorTest() {
        return Stream.of(
                Arguments.of(List.of(), false, false),
                Arguments.of(List.of(HardwareMonitoringDatapointTableRow.random(), HardwareMonitoringDatapointTableRow.random()), true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("constructorTest")
    void constructorTest(List<HardwareMonitoringDatapointTableRow> rows, boolean expectedAnyPresent, boolean expectedAnyProblem) {
        // Act
        var actual = new HardwareMonitoringDatapointTableView(
                rows
        );

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.isAnyPresent()).isEqualTo(expectedAnyPresent);
        assertThat(actual.isAnyProblem()).isEqualTo(expectedAnyProblem);
    }
}
