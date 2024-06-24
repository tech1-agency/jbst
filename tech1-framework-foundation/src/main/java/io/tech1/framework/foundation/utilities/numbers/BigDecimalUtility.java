package io.tech1.framework.foundation.utilities.numbers;

import io.tech1.framework.domain.tuples.TupleRange;
import io.tech1.framework.foundation.utilities.exceptions.ExceptionsMessagesUtility;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

import static io.tech1.framework.domain.asserts.Asserts.assertNonNullOrThrow;
import static io.tech1.framework.domain.constants.BigDecimalConstants.ONE_HUNDRED;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;

@UtilityClass
public class BigDecimalUtility {
    private static final String NUMBER1_PARAM = "number1";
    private static final String NUMBER2_PARAM = "number2";

    public static boolean areValuesEquals(BigDecimal number1, BigDecimal number2) {
        if (isNull(number1) && isNull(number2)) {
            return true;
        }
        if (isNull(number1) || isNull(number2)) {
            return false;
        }
        return number1.compareTo(number2) == 0;
    }

    public static boolean isFirstValueGreater(BigDecimal number1, BigDecimal number2) {
        assertNonNullOrThrow(number1, ExceptionsMessagesUtility.invalidAttribute(NUMBER1_PARAM));
        assertNonNullOrThrow(number2, ExceptionsMessagesUtility.invalidAttribute(NUMBER2_PARAM));
        return number1.compareTo(number2) > 0;
    }

    public static boolean isFirstValueGreaterOrEqual(BigDecimal number1, BigDecimal number2) {
        assertNonNullOrThrow(number1, ExceptionsMessagesUtility.invalidAttribute(NUMBER1_PARAM));
        assertNonNullOrThrow(number2, ExceptionsMessagesUtility.invalidAttribute(NUMBER2_PARAM));
        return number1.compareTo(number2) >= 0;
    }

    public static boolean isFirstValueLesser(BigDecimal number1, BigDecimal number2) {
        assertNonNullOrThrow(number1, ExceptionsMessagesUtility.invalidAttribute(NUMBER1_PARAM));
        assertNonNullOrThrow(number2, ExceptionsMessagesUtility.invalidAttribute(NUMBER2_PARAM));
        return number1.compareTo(number2) < 0;
    }

    public static boolean isFirstValueLesserOrEqual(BigDecimal number1, BigDecimal number2) {
        assertNonNullOrThrow(number1, ExceptionsMessagesUtility.invalidAttribute(NUMBER1_PARAM));
        assertNonNullOrThrow(number2, ExceptionsMessagesUtility.invalidAttribute(NUMBER2_PARAM));
        return number1.compareTo(number2) <= 0;
    }

    public static boolean inRange(BigDecimal number, TupleRange<BigDecimal> range) {
        assertNonNullOrThrow(number, ExceptionsMessagesUtility.invalidAttribute("number"));
        assertNonNullOrThrow(range, ExceptionsMessagesUtility.invalidAttribute("range"));
        return isFirstValueGreater(number, range.from()) && isFirstValueLesser(number, range.to());
    }

    public static boolean inRangeClosed(BigDecimal number, TupleRange<BigDecimal> range) {
        assertNonNullOrThrow(number, ExceptionsMessagesUtility.invalidAttribute("number"));
        assertNonNullOrThrow(range, ExceptionsMessagesUtility.invalidAttribute("range"));
        return isFirstValueGreaterOrEqual(number, range.from()) && isFirstValueLesserOrEqual(number, range.to());
    }

    public static boolean isZero(BigDecimal number) {
        return areValuesEquals(number, ZERO);
    }

    public static boolean isNullOrZero(BigDecimal number) {
        return isNull(number) || isZero(number);
    }

    public static boolean isOneHundred(BigDecimal number) {
        return areValuesEquals(number, ONE_HUNDRED);
    }

    public static boolean isPositive(BigDecimal number) {
        return isFirstValueGreater(number, ZERO);
    }

    public static boolean isPositiveOrZero(BigDecimal number) {
        return isFirstValueGreaterOrEqual(number, ZERO);
    }

    public static boolean isNegative(BigDecimal number) {
        return isFirstValueLesser(number, ZERO);
    }

    public static boolean isNegativeOrZero(BigDecimal number) {
        return isFirstValueLesserOrEqual(number, ZERO);
    }

    public static BigDecimal absOrZero(BigDecimal number) {
        if (isNull(number)) {
            return ZERO;
        } else {
            if (isPositiveOrZero(number)) {
                return number;
            } else {
                return number.negate();
            }
        }
    }

    public static int getNumberOfDigitsAfterTheDecimalPointOrZero(BigDecimal number) {
        return Math.max(0, number.stripTrailingZeros().scale());
    }

    public static int getNumberOfDigitsAfterTheDecimalPointIncludingTrailingZerosOrZero(BigDecimal number) {
        return Math.max(0, number.scale());
    }
}
