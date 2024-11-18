package jbst.foundation.domain.tuples;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jbst.foundation.utilities.numbers.NumbersUtility;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static jbst.foundation.utilities.numbers.RoundingUtility.scale;
import static java.math.BigDecimal.ZERO;

@Data
public class TupleReadableAmount {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final BigDecimal number;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String readable;

    public static TupleReadableAmount zero(int scale) {
        return new TupleReadableAmount(scale(ZERO, scale));
    }

    public static TupleReadableAmount testsHardcoded() {
        return new TupleReadableAmount(new BigDecimal("125.555"));
    }

    public TupleReadableAmount(BigDecimal value) {
        this(value, true, true);
    }

    public TupleReadableAmount(BigDecimal value, boolean numberRequired, boolean readableRequired) {
        this(value, numberRequired, readableRequired, 2);
    }

    public TupleReadableAmount(BigDecimal value, int scale) {
        this(value, true, true, scale);
    }

    public TupleReadableAmount(List<TupleReadableAmount> amounts, int scale) {
        this(
                amounts.stream().map(TupleReadableAmount::getValue).reduce(scale(ZERO, scale), BigDecimal::add),
                scale
        );
    }

    public TupleReadableAmount(BigDecimal value, boolean numberRequired, boolean readableRequired, int scale) {
        this.value = scale(value, scale);
        if (numberRequired) {
            this.number = scale(value, scale);
        } else {
            this.number = null;
        }
        if (readableRequired) {
            this.readable = NumbersUtility.getReadableNumber(this.value, scale);
        } else {
            this.readable = null;
        }
    }
}
