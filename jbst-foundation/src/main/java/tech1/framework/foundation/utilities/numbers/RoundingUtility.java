package tech1.framework.foundation.utilities.numbers;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.nonNull;

@UtilityClass
public class RoundingUtility {
    public static final int DEFAULT_SCALE = 3;

    private static final Map<Integer, DecimalFormat> DFS_BY_SCALE = new ConcurrentHashMap<>();

    public static BigDecimal scale(BigDecimal value) {
        return scale(value, DEFAULT_SCALE);
    }

    public static BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(BigDecimal divider, BigDecimal divisor) {
        return divide(divider, divisor, DEFAULT_SCALE);
    }

    public static BigDecimal divide(BigDecimal divider, BigDecimal divisor, int scale) {
        return divider.divide(divisor, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal divideOrZero(BigDecimal divider, BigDecimal divisor, int scale) {
        if (nonNull(divisor) && divisor.compareTo(ZERO) != 0) {
            return divider.divide(divisor, scale, RoundingMode.HALF_UP);
        } else {
            return ZERO;
        }
    }

    public static BigDecimal divideOrOne(BigDecimal divider, BigDecimal divisor, int scale) {
        try {
            if (nonNull(divisor) && divisor.compareTo(ZERO) != 0) {
                return divider.divide(divisor, scale, RoundingMode.HALF_UP);
            } else {
                return ONE;
            }
        } catch (RuntimeException ex) {
            return ONE;
        }
    }

    public static BigDecimal divideOrFallback(BigDecimal divider, BigDecimal divisor, int scale, BigDecimal fallback) {
        try {
            if (nonNull(divisor) && divisor.compareTo(ZERO) != 0) {
                return divider.divide(divisor, scale, RoundingMode.HALF_UP);
            } else {
                return fallback;
            }
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public static String format(BigDecimal value) {
        return format(value, DEFAULT_SCALE);
    }

    public static String format(BigDecimal value, int scale) {
        var symbols = DecimalFormatSymbols.getInstance();
        symbols.setGroupingSeparator(' ');
        DecimalFormat decimalFormat;
        if (DFS_BY_SCALE.containsKey(scale)) {
            decimalFormat = DFS_BY_SCALE.get(scale);
        } else {
            var pattern = "###,###." + IntStream.range(0, scale).mapToObj(i -> "#").collect(Collectors.joining());
            decimalFormat = new DecimalFormat(pattern, symbols);
            DFS_BY_SCALE.put(scale, decimalFormat);
        }
        return decimalFormat.format(value).replace(".", ",");
    }
}
