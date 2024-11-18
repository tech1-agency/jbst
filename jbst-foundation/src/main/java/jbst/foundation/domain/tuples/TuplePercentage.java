package jbst.foundation.domain.tuples;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static jbst.foundation.domain.constants.JbstConstants.BigDecimals.ONE_HUNDRED;
import static jbst.foundation.utilities.numbers.RoundingUtility.divideOrZero;
import static jbst.foundation.utilities.numbers.RoundingUtility.scale;

public record TuplePercentage(
        BigDecimal value,
        BigDecimal percentage
) {

    public static TuplePercentage of(BigDecimal value, BigDecimal maxValue, int valueScale, int percentageScale) {
        return new TuplePercentage(
                scale(value, valueScale),
                divideOrZero(value.abs().multiply(ONE_HUNDRED), maxValue, percentageScale)
        );
    }

    public static TuplePercentage progressTuplePercentage(BigDecimal value, BigDecimal maxValue) {
        return of(value, maxValue, 2, 2);
    }

    public static TuplePercentage progressTuplePercentage(long value, long maxValue) {
        return progressTuplePercentage(new BigDecimal(value), new BigDecimal(maxValue));
    }

    public static TuplePercentage zero() {
        return progressTuplePercentage(ZERO, ONE_HUNDRED);
    }

    public static TuplePercentage oneHundred() {
        return progressTuplePercentage(ONE_HUNDRED, ONE_HUNDRED);
    }
}
