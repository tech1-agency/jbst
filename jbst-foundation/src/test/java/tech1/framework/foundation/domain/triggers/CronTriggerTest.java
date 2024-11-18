package tech1.framework.foundation.domain.triggers;

import tech1.framework.foundation.domain.tests.runners.AbstractSerializationDeserializationRunner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CronTriggerTest extends AbstractSerializationDeserializationRunner {
    private static final CronTrigger CRON_TRIGGER = new CronTrigger();

    @Override
    protected String getFileName() {
        return "trigger-cron.json";
    }

    @Override
    protected String getFolder() {
        return "triggers";
    }

    @Test
    void serializeTest() {
        // Act
        var json = this.writeValueAsString(CRON_TRIGGER);

        // Assert
        assertThat(json).isEqualTo(this.readFile());
    }
}
