package jbst.foundation.utilities.random;

import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import jbst.foundation.domain.constants.BigDecimalConstants;
import jbst.foundation.domain.constants.BigIntegerConstants;
import jbst.foundation.domain.exceptions.random.IllegalEnumException;
import jbst.foundation.domain.tests.enums.EnumOneValueUnderTests;
import jbst.foundation.domain.tests.enums.EnumUnderTests;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static java.time.ZoneId.getAvailableZoneIds;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static jbst.foundation.domain.tests.constants.TestsJunitConstants.RANDOM_ITERATIONS_COUNT;
import static jbst.foundation.domain.tests.constants.TestsJunitConstants.SMALL_ITERATIONS_COUNT;
import static jbst.foundation.domain.tests.enums.EnumUnderTests.*;
import static jbst.foundation.utilities.random.RandomUtility.*;

@Slf4j
class RandomUtilityTest {

    private static Stream<Arguments> oneTest() {
        return Stream.of(
                Arguments.of(Short.class, 1),
                Arguments.of(Integer.class, 1),
                Arguments.of(Long.class, 1L),
                Arguments.of(Double.class, 1.0d),
                Arguments.of(BigDecimal.class, ONE)
        );
    }

    private static Stream<Arguments> containsPrimitiveWrapperTest() {
        return Stream.of(
                Arguments.of(Short.class, true),
                Arguments.of(Boolean.class, true),
                Arguments.of(Integer.class, true),
                Arguments.of(Long.class, true),
                Arguments.of(Double.class, true),
                Arguments.of(BigDecimal.class, true),
                Arguments.of(Float.class, false),
                Arguments.of(String.class, false)
        );
    }

    @ParameterizedTest
    @MethodSource("oneTest")
    void oneTest(Class<? extends Number> clazz, Number expected) {
        // Act
        var actual = one(clazz);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void oneTestException() {
        // Act
        var throwable = catchThrowable(() -> one(Float.class));

        // Assert
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unexpected clazz: java.lang.Float");
    }

    @Test
    void randomShortTest() {
        // Act
        var actual = randomShort();

        // Assert
        assertThat(actual).isNotNull();
    }

    @Test
    void randomBooleanTest() {
        // Act
        var actual = randomBoolean();

        // Assert
        LOGGER.info("randomBoolean is ignored: " + actual);
    }

    @Test
    void randomDoubleTest() {
        // Act
        var actual = randomDouble();

        // Assert
        assertThat(actual).isNotNull();
    }

    @Test
    void randomIntegerTest() {
        // Act
        var actual = randomInteger();

        // Assert
        assertThat(actual).isNotNull();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomIntegerGreaterThanZeroTest() {
        // Act
        var actual = randomIntegerGreaterThanZero();

        // Assert
        assertThat(actual).isPositive();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomIntegerLessThanZeroTest() {
        // Act
        var actual = randomIntegerLessThanZero();

        // Assert
        assertThat(actual).isNegative();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomIntegerGreaterThanZeroByBoundsTest() {
        // Arrange
        var lowerBound = randomIntegerGreaterThanZeroByBounds(50, 75);
        var upperBound = randomIntegerGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomIntegerGreaterThanZeroByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(lowerBound)
                .isLessThanOrEqualTo(upperBound);
    }

    @Test
    void randomLongTest() {
        // Act
        var actual = randomLong();

        // Assert
        assertThat(actual).isNotNull();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomLongGreaterThanZeroTest() {
        // Act
        var actual = randomLongGreaterThanZero();

        // Assert
        assertThat(actual).isPositive();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomLongLessThanZeroTest() {
        // Act
        var actual = randomLongLessThanZero();

        // Assert
        assertThat(actual).isNegative();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomLongGreaterThanZeroByBoundsTest() {
        // Arrange
        var lowerBound = randomLongGreaterThanZeroByBounds(50, 75);
        var upperBound = randomLongGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomLongGreaterThanZeroByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(lowerBound)
                .isLessThanOrEqualTo(upperBound);
    }

    @Test
    void randomBigDecimalTest() {
        // Act
        var actual = randomBigDecimal();

        // Assert
        assertThat(actual).isNotNull();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigDecimalGreaterThanZeroTest() {
        // Act
        var actual = randomBigDecimalGreaterThanZero();

        // Assert
        assertThat(actual).isGreaterThan(BigDecimal.ZERO);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigDecimalLessThanZeroTest() {
        // Act
        var actual = randomBigDecimalLessThanZero();

        // Assert
        assertThat(actual).isLessThan(BigDecimal.ZERO);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigDecimalGreaterThanZeroByBoundsTest() {
        // Arrange
        long lowerBound = randomLongGreaterThanZeroByBounds(50, 75);
        long upperBound = randomLongGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomBigDecimalGreaterThanZeroByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(BigDecimal.valueOf(lowerBound))
                .isLessThanOrEqualTo(BigDecimal.valueOf(upperBound));
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigDecimalLessThanZeroByBoundsTest() {
        // Arrange
        long lowerBound = randomLongGreaterThanZeroByBounds(50, 75);
        long upperBound = randomLongGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomBigDecimalLessThanZeroByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(BigDecimal.valueOf(upperBound).multiply(BigDecimalConstants.MINUS_ONE))
                .isLessThanOrEqualTo(BigDecimal.valueOf(lowerBound).multiply(BigDecimalConstants.MINUS_ONE));
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigDecimalByBoundsTest() {
        // Arrange
        long lowerBound = randomLongGreaterThanZeroByBounds(50, 75);
        long upperBound = randomLongGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomBigDecimalByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(BigDecimal.valueOf(upperBound).multiply(BigDecimalConstants.MINUS_ONE))
                .isLessThanOrEqualTo(BigDecimal.valueOf(upperBound));
    }

    @Test
    void randomBigIntegerTest() {
        // Act
        var actual = randomBigInteger();

        // Assert
        assertThat(actual).isNotNull();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigIntegerGreaterThanZeroTest() {
        // Act
        var actual = randomBigIntegerGreaterThanZero();

        // Assert
        assertThat(actual).isGreaterThan(BigInteger.ZERO);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigIntegerLessThanZeroTestt() {
        // Act
        var actual = randomBigIntegerLessThanZero();

        // Assert
        assertThat(actual).isLessThan(BigInteger.ZERO);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigIntegerGreaterThanZeroByBoundsTest() {
        // Arrange
        long lowerBound = randomLongGreaterThanZeroByBounds(50, 75);
        long upperBound = randomLongGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomBigIntegerGreaterThanZeroByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(BigInteger.valueOf(lowerBound))
                .isLessThanOrEqualTo(BigInteger.valueOf(upperBound));
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigIntegerLessThanZeroByBoundsTest() {
        // Arrange
        long lowerBound = randomLongGreaterThanZeroByBounds(50, 75);
        long upperBound = randomLongGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomBigIntegerLessThanZeroByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(BigInteger.valueOf(upperBound).multiply(BigIntegerConstants.MINUS_ONE))
                .isLessThanOrEqualTo(BigInteger.valueOf(lowerBound).multiply(BigIntegerConstants.MINUS_ONE));
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomBigIntegerByBoundsTest() {
        // Arrange
        long lowerBound = randomLongGreaterThanZeroByBounds(50, 75);
        long upperBound = randomLongGreaterThanZeroByBounds(77, 100);

        // Act
        var actual = randomBigIntegerByBounds(lowerBound, upperBound);

        // Assert
        assertThat(actual)
                .isGreaterThanOrEqualTo(BigInteger.valueOf(upperBound).multiply(BigIntegerConstants.MINUS_ONE))
                .isLessThanOrEqualTo(BigInteger.valueOf(upperBound));
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomStringTest() {
        // Act
        var actual = randomString();

        // Assert
        assertThat(actual).hasSize(32);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomStringLetterOrNumbersOnlyTest() {
        // Arrange
        var regex = "[^a-z0-9 ]";
        var size = 40;

        // Act
        var actual = randomStringLetterOrNumbersOnly(size);

        // Assert
        assertThat(actual).hasSize(size);
        assertThat(Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(actual).find()).isFalse();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomIPv4Test() {
        // Act
        var actual = randomIPv4();

        // Assert
        var ipv4 = List.of(actual.split("\\."));
        assertThat(ipv4).hasSize(4);
        ipv4.forEach(element -> {
            var slot = Integer.valueOf(element);
            assertThat(slot).isNotNull();
            assertThat(slot).isNotNegative();
            assertThat(slot).isLessThan(256);
        });
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomServerURLTest() {
        // Arrange
        var pattern = Pattern.compile("^"
                + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
                + "|"
                + "localhost" // localhost
                + "|"
                + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // IP
                + ":"
                + "[0-9]{1,5}$"); // Port

        // Act
        var actual = randomServerURL();

        // Assert
        assertThat(actual).isNotNull();
        assertThat(pattern.matcher(actual.split("://")[1]).matches()).isTrue();
    }

    @Test
    void randomStringsAsListTest() {
        // Arrange
        int size = randomIntegerGreaterThanZeroByBounds(1, 5);
        var elementLength = 32;

        // Act
        var actual = randomStringsAsList(size);

        // Assert
        assertThat(actual).hasSize(size);
        actual.forEach(element -> assertThat(element).hasSize(elementLength));
    }

    @Test
    void randomStringsAsSetTest() {
        // Arrange
        int size = randomIntegerGreaterThanZeroByBounds(1, 5);
        var elementLength = 32;

        // Act
        var actual = randomStringsAsSet(size);

        // Assert
        assertThat(actual.getClass()).isEqualTo(HashSet.class);
        assertThat(actual).hasSize(size);
        actual.forEach(element -> assertThat(element).hasSize(elementLength));
    }

    @Test
    void randomStringsAsArrayTest() {
        // Arrange
        int size = randomIntegerGreaterThanZeroByBounds(1, 5);
        var elementLength = 32;

        // Act
        var actual = randomStringsAsArray(size);

        // Assert
        assertThat(actual).hasSize(size);
        asList(actual).forEach(element -> assertThat(element).hasSize(elementLength));
    }

    @Test
    void randomElementListTest() {
        // Arrange
        var list = List.of(
                EXAMPLE_1,
                EXAMPLE_2,
                EXAMPLE_3,
                EXAMPLE_4
        );

        // Act
        var actual = randomElement(list);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(list).contains(actual);
    }

    @Test
    void randomElementSetTest() {
        // Arrange
        var set = Set.of(
                EXAMPLE_1,
                EXAMPLE_2,
                EXAMPLE_3,
                EXAMPLE_4
        );

        // Act
        var actual = randomElement(set);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(set).contains(actual);
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomElementExceptList1Test() {
        // Act
        var actual = randomElementExcept(
                List.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_2, EXAMPLE_3),
                List.of(EXAMPLE_4)
        );

        // Assert
        assertThat(actual)
                .isIn(Set.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_3))
                .isNotIn(Set.of(EXAMPLE_4));
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomElementExceptList2Test() {
        // Act
        var actual = randomElementExcept(
                List.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_2, EXAMPLE_3),
                List.of(EXAMPLE_2, EXAMPLE_4)
        );

        // Assert
        assertThat(actual)
                .isIn(Set.of(EXAMPLE_1, EXAMPLE_3))
                .isNotIn(Set.of(EXAMPLE_2, EXAMPLE_4));
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomElementExceptList3Test() {
        // Act
        var actual = randomElementExcept(
                List.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_2, EXAMPLE_3),
                List.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_2, EXAMPLE_3)
        );

        // Assert
        assertThat(actual).isNull();
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomElementExceptSetTest() {
        // Act
        var actual = randomElementExcept(
                Set.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_3),
                Set.of(EXAMPLE_4)
        );

        // Assert
        assertThat(actual)
                .isIn(Set.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_3))
                .isNotIn(Set.of(EXAMPLE_4));
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomElementExceptSet2Test() {
        // Act
        var actual = randomElementExcept(
                Set.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_3),
                Set.of(EXAMPLE_2, EXAMPLE_4)
        );

        // Assert
        assertThat(actual)
                .isIn(Set.of(EXAMPLE_1, EXAMPLE_3))
                .isNotIn(Set.of(EXAMPLE_2, EXAMPLE_4));
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomElementExceptSet3Test() {
        // Act
        var actual = randomElementExcept(
                Set.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_3),
                Set.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_3)
        );

        // Assert
        assertThat(actual).isNull();
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomLocalDateTest() {
        // Act
        var actual = randomLocalDate();

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.getYear()).isGreaterThanOrEqualTo(2000);
        assertThat(actual.getYear()).isLessThanOrEqualTo(LocalDate.now().getYear());
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomLocalDateByBoundsTest() {
        // Arrange
        var minYear = randomIntegerGreaterThanZeroByBounds(2000, 2002);
        var maxYear = randomIntegerGreaterThanZeroByBounds(2020, 2022);

        // Act
        var actual = randomLocalDateByBounds(minYear, maxYear);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.getYear()).isGreaterThanOrEqualTo(2000);
        assertThat(actual.getYear()).isLessThanOrEqualTo(2022);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomLocalDateTimeTest() {
        // Act
        var actual = randomLocalDateTime();

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.getYear()).isGreaterThanOrEqualTo(2000);
        assertThat(actual.getYear()).isLessThanOrEqualTo(LocalDate.now().getYear());
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomLocalDateTimeByBoundsTest() {
        // Arrange
        var minYear = randomIntegerGreaterThanZeroByBounds(2000, 2002);
        var maxYear = randomIntegerGreaterThanZeroByBounds(2020, 2022);

        // Act
        var actual = randomLocalDateTimeByBounds(minYear, maxYear);

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.getYear()).isGreaterThanOrEqualTo(2000);
        assertThat(actual.getYear()).isLessThanOrEqualTo(2022);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomDateTest() {
        // Act
        var actual = randomDate();

        // Assert
        assertThat(actual).isNotNull();

        var calendar = Calendar.getInstance();
        calendar.setTime(actual);
        assertThat(calendar.get(Calendar.YEAR)).isGreaterThanOrEqualTo(2000);
        assertThat(calendar.get(Calendar.YEAR)).isLessThanOrEqualTo(LocalDate.now().getYear());
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomDateByBoundsTest() {
        // Arrange
        var minYear = randomIntegerGreaterThanZeroByBounds(2000, 2002);
        var maxYear = randomIntegerGreaterThanZeroByBounds(2020, 2022);

        // Act
        var actual = randomDateByBounds(minYear, maxYear);

        // Assert
        assertThat(actual).isNotNull();

        var calendar = Calendar.getInstance();
        calendar.setTime(actual);
        assertThat(calendar.get(Calendar.YEAR)).isGreaterThanOrEqualTo(2000);
        assertThat(calendar.get(Calendar.YEAR)).isLessThanOrEqualTo(LocalDate.now().getYear());
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomEnumTest() {
        // Act
        var actual1 = randomEnum(EnumUnderTests.class);
        EnumUnderTests actual2 = randomEnumWildcard(EnumUnderTests.class);

        // Assert
        assertThat(EnumUnderTests.values()).contains(actual1);
        assertThat(EnumUnderTests.values()).contains(actual2);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomEnumExceptExceptionTest() {
        // Act
        var throwable1 = catchThrowable(() -> randomEnumExcept(EnumOneValueUnderTests.class, EnumOneValueUnderTests.ONE_VALUE));
        var throwable2 = catchThrowable(() -> randomEnumExceptWildcard(EnumOneValueUnderTests.class, EnumOneValueUnderTests.ONE_VALUE));

        // Assert
        assertThat(throwable1).isInstanceOf(IllegalEnumException.class);
        assertThat(throwable2).isInstanceOf(IllegalEnumException.class);
        var message = "Please check enum: class tech1.framework.foundation.domain.tests.enums.EnumOneValueUnderTests";
        assertThat(throwable1.getMessage()).isEqualTo(message);
        assertThat(throwable2.getMessage()).isEqualTo(message);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomEnumExceptWildcardTest() {
        // Arrange
        var enumValues = List.of(EnumUnderTests.values());
        var clazz = EnumUnderTests.class;
        var randomEnum = randomEnum(clazz);

        // Act
        var actual1 = randomEnumExcept(clazz, randomEnum);
        var actual2 = randomEnumExceptWildcard(clazz, randomEnum);

        // Assert
        assertThat(enumValues)
                .contains(randomEnum)
                .contains(actual2)
                .contains(actual1);
        assertThat(actual2).isNotEqualTo(randomEnum);
        assertThat(actual1).isNotEqualTo(randomEnum);
    }

    @RepeatedTest(RANDOM_ITERATIONS_COUNT)
    void randomEnumExceptCaseAsListTest() {
        // Arrange
        var enumValues = List.of(EnumUnderTests.values());
        var clazz = EnumUnderTests.class;
        var randomEnum1 = randomEnum(clazz);
        var randomEnum2 = randomEnumExcept(clazz, randomEnum1);
        var randomEnums = List.of(randomEnum1, randomEnum2);

        // Act
        var actual1 = randomEnumExcept(clazz, randomEnums);
        var actual2 = randomEnumExceptWildcard(clazz, randomEnums);

        // Assert
        assertThat(enumValues)
                .contains(randomEnum1)
                .contains(randomEnum2)
                .contains(actual1)
                .contains(actual2);
        assertThat(actual1).isNotEqualTo(randomEnum1);
        assertThat(actual2).isNotEqualTo(randomEnum1);
        assertThat(actual1).isNotEqualTo(randomEnum2);
        assertThat(actual2).isNotEqualTo(randomEnum2);
        assertThat(randomEnum1).isNotEqualTo(randomEnum2);
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomEnumExceptCaseAsListExceptionTest() {
        // Arrange
        var clazz = EnumUnderTests.class;
        var allPossibleEnumValues = List.of(EXAMPLE_1, EXAMPLE_2, EXAMPLE_3, EXAMPLE_4);

        // Act
        var throwable1 = catchThrowable(() -> randomEnumExcept(clazz, allPossibleEnumValues));
        var throwable2 = catchThrowable(() -> randomEnumExceptWildcard(clazz, allPossibleEnumValues));

        // Assert
        assertThat(throwable1).isInstanceOf(IllegalEnumException.class);
        assertThat(throwable2).isInstanceOf(IllegalEnumException.class);
        var message = "Please check enum: class tech1.framework.foundation.domain.tests.enums.EnumUnderTests";
        assertThat(throwable1.getMessage()).isEqualTo(message);
        assertThat(throwable2.getMessage()).isEqualTo(message);
    }

    @ParameterizedTest
    @MethodSource("containsPrimitiveWrapperTest")
    void containsPrimitiveWrapperTest(Class<?> primitiveWrapper, boolean expected) {
        // Act
        var actualContains = containsPrimitiveWrapper(primitiveWrapper);

        // Assert
        assertThat(actualContains).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("containsPrimitiveWrapperTest")
    void randomListOfPrimitiveWrappersTest(Class<?> primitiveWrapper, boolean containsPrimitiveWrrapper) {
        // Arrange
        int size = randomIntegerGreaterThanZeroByBounds(2, 5);

        // Act
        var actual = randomListOfPrimitiveWrappers(primitiveWrapper, size);

        // Assert
        assertThat(actual).isNotNull();
        if (containsPrimitiveWrrapper) {
            actual.forEach(item -> {
                assertThat(item).isNotNull();
                assertThat(item.getClass()).isEqualTo(primitiveWrapper);
            });
            assertThat(actual).hasSize(size);
        } else {
            assertThat(actual).isEmpty();
        }
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomMethodTest() {
        // Act
        var actual = randomMethod();

        // Assert
        assertThat(actual.getName()).isEqualTo("finalize");
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomZoneIdTest() {
        // Act
        var actual = randomZoneId();

        // Assert
        assertThat(getAvailableZoneIds()).contains(actual.getId());
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomTimeZoneTest() {
        // Act
        var actual = randomTimeZone();

        // Assert
        assertThat(getAvailableZoneIds()).contains(actual.toZoneId().getId());
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomTimeUnitTest() {
        // Act
        var timeUnit = randomTimeUnit();

        // Assert
        assertThat(TimeUnit.values()).contains(timeUnit);
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomChronoUnitTest() {
        // Act
        var chronoUnit = randomChronoUnit();

        // Assert
        assertThat(ChronoUnit.values()).contains(chronoUnit);
    }

    @RepeatedTest(SMALL_ITERATIONS_COUNT)
    void randomFeignExceptionTest() {
        // Act
        var actual = randomFeignException();

        // Assert
        assertThat(actual).isNotNull();
        assertThat(actual.request().httpMethod()).isEqualTo(Request.HttpMethod.GET);
        assertThat(actual.request().url()).isEqualTo("/endpoint");
    }
}
