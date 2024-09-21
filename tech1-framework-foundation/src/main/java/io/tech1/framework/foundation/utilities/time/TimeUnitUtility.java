package io.tech1.framework.foundation.utilities.time;

import lombok.experimental.UtilityClass;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class TimeUnitUtility {

    public static TimeUnit toTimeUnit(ChronoUnit chronoUnit) {
        if (chronoUnit == ChronoUnit.NANOS) {
            return TimeUnit.NANOSECONDS;
        } else if (chronoUnit == ChronoUnit.MICROS) {
            return TimeUnit.MICROSECONDS;
        } else if (chronoUnit == ChronoUnit.MILLIS) {
            return TimeUnit.MILLISECONDS;
        } else if (chronoUnit == ChronoUnit.SECONDS) {
            return TimeUnit.SECONDS;
        } else if (chronoUnit == ChronoUnit.MINUTES) {
            return TimeUnit.MINUTES;
        } else if (chronoUnit == ChronoUnit.HOURS) {
            return TimeUnit.HOURS;
        } else if (chronoUnit == ChronoUnit.DAYS) {
            return TimeUnit.DAYS;
        } else {
            throw new IllegalArgumentException("Unsupported ChronoUnit: %s".formatted(chronoUnit));
        }
    }
}
