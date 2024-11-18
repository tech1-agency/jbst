package tech1.framework.foundation.domain.hardware.monitoring;

import com.jayway.jsonpath.JsonPath;
import tech1.framework.foundation.domain.hardware.memories.CpuMemory;
import tech1.framework.foundation.domain.hardware.memories.GlobalMemory;
import tech1.framework.foundation.domain.hardware.memories.HeapMemory;
import tech1.framework.foundation.domain.tests.runners.AbstractFolderSerializationRunner;
import lombok.SneakyThrows;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HardwareMonitoringDatapointTest extends AbstractFolderSerializationRunner {

    @Override
    protected String getFolder() {
        return "hardware/monitoring";
    }

    @SneakyThrows
    @RepeatedTest(5)
    void integrationTest() {
        // Arrange
        var threshold = new BigDecimal("50");
        var datapoint = new HardwareMonitoringDatapoint(
                new GlobalMemory(
                        5957144576L,
                        17179869184L,
                        6144655360L,
                        7516192768L,
                        17367379968L,
                        24696061952L
                ),
                new CpuMemory(new BigDecimal("53.28")),
                new HeapMemory(
                        268435456L,
                        43194304L,
                        4294967296L,
                        268435456L
                )
        );
        var thresholds = new HardwareMonitoringThresholds(
                Map.of(
                        HardwareName.CPU, threshold,
                        HardwareName.HEAP, threshold,
                        HardwareName.SERVER, threshold,
                        HardwareName.SWAP, threshold,
                        HardwareName.VIRTUAL, threshold
                )
        );
        var tableView = datapoint.tableView(thresholds);

        // Act
        var datapointJson = this.writeValueAsString(datapoint);
        var tableViewJson = this.writeValueAsString(tableView);

        // Assert
        var datapointDC = JsonPath.parse(datapointJson);
        assertThat(datapointDC).isNotNull();
        assertThat(datapointDC.read("$.timestamp", long.class)).isPositive();
        assertThat(datapointDC.read("$.unit", String.class)).isEqualTo("GB");
        assertThat(datapointDC.read("$.global.a.value", BigDecimal.class)).isEqualTo(new BigDecimal("10.45"));
        assertThat(datapointDC.read("$.global.a.percentage", BigDecimal.class)).isEqualTo(new BigDecimal("65.3"));
        assertThat(datapointDC.read("$.global.b.value", BigDecimal.class)).isEqualTo(new BigDecimal("5.72"));
        assertThat(datapointDC.read("$.global.b.percentage", BigDecimal.class)).isEqualTo(new BigDecimal("81.8"));
        assertThat(datapointDC.read("$.global.c.value", BigDecimal.class)).isEqualTo(new BigDecimal("16.17"));
        assertThat(datapointDC.read("$.global.c.percentage", BigDecimal.class)).isEqualTo(new BigDecimal("70.3"));
        assertThat(datapointDC.read("$.cpu", BigDecimal.class)).isEqualTo("53.28");
        assertThat(datapointDC.read("$.heap.value", BigDecimal.class)).isEqualTo(new BigDecimal("0.04"));
        assertThat(datapointDC.read("$.heap.percentage", BigDecimal.class)).isEqualTo(new BigDecimal("1.0"));
        assertThat(datapointDC.read("$.maxValues.server", Long.class)).isEqualTo(17179869184L);
        assertThat(datapointDC.read("$.maxValues.swap", Long.class)).isEqualTo(7516192768L);
        assertThat(datapointDC.read("$.maxValues.virtual", Long.class)).isEqualTo(24696061952L);
        assertThat(datapointDC.read("$.maxValues.heap", Long.class)).isEqualTo(4294967296L);
        var tableViewDC = JsonPath.parse(tableViewJson);
        assertThat(tableViewDC).isNotNull();
        assertThat(tableViewDC.read("$.anyPresent", boolean.class)).isTrue();
        assertThat(tableViewDC.read("$.anyProblem", boolean.class)).isTrue();
        assertThat(tableViewDC.read("$.rows[0].hardwareName", String.class)).isEqualTo("CPU");
        assertThat(tableViewDC.read("$.rows[0].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[0].usage", BigDecimal.class)).isEqualTo(new BigDecimal("53.28"));
        assertThat(tableViewDC.read("$.rows[0].value", String.class)).isEmpty();
        assertThat(tableViewDC.read("$.rows[1].hardwareName", String.class)).isEqualTo("Heap");
        assertThat(tableViewDC.read("$.rows[1].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[1].usage", BigDecimal.class)).isEqualTo(new BigDecimal("1.0"));
        assertThat(tableViewDC.read("$.rows[1].value", String.class)).isEqualTo("0.04 GB of 4.00 GB");
        assertThat(tableViewDC.read("$.rows[2].hardwareName", String.class)).isEqualTo("Server");
        assertThat(tableViewDC.read("$.rows[2].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[2].usage", BigDecimal.class)).isEqualTo(new BigDecimal("65.3"));
        assertThat(tableViewDC.read("$.rows[2].value", String.class)).isEqualTo("10.45 GB of 16.00 GB");
        assertThat(tableViewDC.read("$.rows[3].hardwareName", String.class)).isEqualTo("Swap");
        assertThat(tableViewDC.read("$.rows[3].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[3].usage", BigDecimal.class)).isEqualTo(new BigDecimal("81.8"));
        assertThat(tableViewDC.read("$.rows[3].value", String.class)).isEqualTo("5.72 GB of 7.00 GB");
        assertThat(tableViewDC.read("$.rows[4].hardwareName", String.class)).isEqualTo("Virtual");
        assertThat(tableViewDC.read("$.rows[4].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[4].usage", BigDecimal.class)).isEqualTo(new BigDecimal("70.3"));
        assertThat(tableViewDC.read("$.rows[4].value", String.class)).isEqualTo("16.17 GB of 23.00 GB");
    }

    @SneakyThrows
    @Test
    void zeroUsageTest() {
        // Arrange
        var threshold = new BigDecimal("50");
        var datapoint = HardwareMonitoringDatapoint.zeroUsage();
        var thresholds = new HardwareMonitoringThresholds(
                Map.of(
                        HardwareName.CPU, threshold,
                        HardwareName.HEAP, threshold,
                        HardwareName.SERVER, threshold,
                        HardwareName.SWAP, threshold,
                        HardwareName.VIRTUAL, threshold
                )
        );
        var tableView = datapoint.tableView(thresholds);

        // Act
        var datapointJson = this.writeValueAsString(datapoint);
        var tableViewJson = this.writeValueAsString(tableView);

        // Assert
        var datapointDC = JsonPath.parse(datapointJson);
        assertThat(datapointDC).isNotNull();
        assertThat(datapointDC.read("$.timestamp", long.class)).isPositive();
        assertThat(datapointDC.read("$.unit", String.class)).isEqualTo("GB");
        assertThat(datapointDC.read("$.global.a.value", BigDecimal.class)).isEqualTo(new BigDecimal("0.0"));
        assertThat(datapointDC.read("$.global.a.percentage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(datapointDC.read("$.global.b.value", BigDecimal.class)).isEqualTo(new BigDecimal("0.0"));
        assertThat(datapointDC.read("$.global.b.percentage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(datapointDC.read("$.global.c.value", BigDecimal.class)).isEqualTo(new BigDecimal("0.0"));
        assertThat(datapointDC.read("$.global.c.percentage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(datapointDC.read("$.cpu", BigDecimal.class)).isEqualTo("0.0");
        assertThat(datapointDC.read("$.heap.value", BigDecimal.class)).isEqualTo(new BigDecimal("0.0"));
        assertThat(datapointDC.read("$.heap.percentage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(datapointDC.read("$.maxValues.server", Long.class)).isZero();
        assertThat(datapointDC.read("$.maxValues.swap", Long.class)).isZero();
        assertThat(datapointDC.read("$.maxValues.virtual", Long.class)).isZero();
        assertThat(datapointDC.read("$.maxValues.heap", Long.class)).isZero();
        var tableViewDC = JsonPath.parse(tableViewJson);
        assertThat(tableViewDC).isNotNull();
        assertThat(tableViewDC.read("$.anyPresent", boolean.class)).isTrue();
        assertThat(tableViewDC.read("$.anyProblem", boolean.class)).isFalse();
        assertThat(tableViewDC.read("$.rows[0].hardwareName", String.class)).isEqualTo("CPU");
        assertThat(tableViewDC.read("$.rows[0].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[0].usage", BigDecimal.class)).isEqualTo(new BigDecimal("0.0"));
        assertThat(tableViewDC.read("$.rows[0].value", String.class)).isEmpty();
        assertThat(tableViewDC.read("$.rows[1].hardwareName", String.class)).isEqualTo("Heap");
        assertThat(tableViewDC.read("$.rows[1].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[1].usage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(tableViewDC.read("$.rows[1].value", String.class)).isEqualTo("0.00 GB of 0.00 GB");
        assertThat(tableViewDC.read("$.rows[2].hardwareName", String.class)).isEqualTo("Server");
        assertThat(tableViewDC.read("$.rows[2].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[2].usage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(tableViewDC.read("$.rows[2].value", String.class)).isEqualTo("0.00 GB of 0.00 GB");
        assertThat(tableViewDC.read("$.rows[3].hardwareName", String.class)).isEqualTo("Swap");
        assertThat(tableViewDC.read("$.rows[3].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[3].usage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(tableViewDC.read("$.rows[3].value", String.class)).isEqualTo("0.00 GB of 0.00 GB");
        assertThat(tableViewDC.read("$.rows[4].hardwareName", String.class)).isEqualTo("Virtual");
        assertThat(tableViewDC.read("$.rows[4].timestamp", long.class)).isPositive();
        assertThat(tableViewDC.read("$.rows[4].usage", BigDecimal.class)).isEqualTo(BigDecimal.ZERO);
        assertThat(tableViewDC.read("$.rows[4].value", String.class)).isEqualTo("0.00 GB of 0.00 GB");
    }
}
