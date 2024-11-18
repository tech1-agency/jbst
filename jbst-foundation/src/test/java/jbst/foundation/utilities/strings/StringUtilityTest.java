package jbst.foundation.utilities.strings;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static jbst.foundation.utilities.random.RandomUtility.*;
import static jbst.foundation.utilities.strings.StringUtility.*;
import static org.assertj.core.api.Assertions.assertThat;

class StringUtilityTest {

    private static Stream<Arguments> isNullOrEmptyTest() {
        return Stream.of(
                Arguments.of(null, true),
                Arguments.of("null", false),
                Arguments.of("", true),
                Arguments.of(" ", false),
                Arguments.of("   ", false),
                Arguments.of("        ", false),
                Arguments.of("     t1   ", false),
                Arguments.of(randomString(), false)
        );
    }

    private static Stream<Arguments> isNullOrBlankTest() {
        return Stream.of(
                Arguments.of(null, true),
                Arguments.of("null", false),
                Arguments.of("", true),
                Arguments.of(" ", true),
                Arguments.of("   ", true),
                Arguments.of("        ", true),
                Arguments.of("     t1   ", false),
                Arguments.of(randomString(), false)
        );
    }

    private static Stream<Arguments> getShortenValueOrUndefinedTest() {
        return Stream.of(
                Arguments.of(null, randomIntegerGreaterThanZero(), "[?]"),
                Arguments.of(null, randomIntegerLessThanZero(), "[?]"),
                Arguments.of("nil", 4, "nil"),
                Arguments.of("nil", 3, "nil"),
                Arguments.of("nil", 0, "nil"),
                Arguments.of("nil", -1, "nil"),
                Arguments.of("null", 4, "null"),
                Arguments.of("null", 10, "null"),
                Arguments.of("null", 3, "..."),
                Arguments.of("null", 2, "..."),
                Arguments.of("null", 1, "..."),
                Arguments.of("null", 0, "..."),
                Arguments.of("null", -1, "...")
        );
    }

    private static Stream<Arguments> convertCamelCaseToSplitArgs() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("abc", "Abc"),
                Arguments.of("abcDefGht", "Abc def ght"),
                Arguments.of("invitation", "Invitation"),
                Arguments.of("invitationCodeRequest", "Invitation code request"),
                Arguments.of("invitationCodeRequestLong", "Invitation code request long"),
                Arguments.of("invitationCodeRequestLongLongLong", "Invitation code request long long long")
        );
    }

    private static Stream<Arguments> hasLengthTest() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("null", true),
                Arguments.of("", false),
                Arguments.of(" ", true),
                Arguments.of("   ", true),
                Arguments.of("        ", true),
                Arguments.of("     t1   ", true),
                Arguments.of(randomString(), true)
        );
    }

    @ParameterizedTest
    @MethodSource("isNullOrEmptyTest")
    void isNullOrEmptyTest(String value, boolean expected) {
        // Act
        var actual = isNullOrEmpty(value);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("isNullOrBlankTest")
    void isNullOrBlankTest(String value, boolean expected) {
        // Act
        var actual = isNullOrBlank(value);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("hasLengthTest")
    void hasLengthTest(String value, boolean expected) {
        // Act
        var actual = hasLength(value);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("getShortenValueOrUndefinedTest")
    void getShortenValueOrUndefinedTest(String value, int maxLength, String expected) {
        // Act
        var actual = getShortenValueOrUndefined(value, maxLength);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("convertCamelCaseToSplitArgs")
    void convertCamelCaseToSplitTest(String value, String expected) {
        // Act
        var actual = convertCamelCaseToSplit(value);

        // Assert
        assertThat(actual).isEqualTo(expected);
    }
}
