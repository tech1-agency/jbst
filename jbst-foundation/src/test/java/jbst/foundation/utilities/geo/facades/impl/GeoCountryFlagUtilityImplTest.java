package jbst.foundation.utilities.geo.facades.impl;

import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;
import jbst.foundation.domain.constants.JbstConstants;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.tests.constants.TestsFlagsConstants;
import jbst.foundation.utilities.geo.facades.GeoCountryFlagUtility;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class GeoCountryFlagUtilityImplTest {

    private static Stream<Arguments> getFlagEmojiTest() {
        return Stream.of(
                Arguments.of(null, null, TestsFlagsConstants.UNKNOWN),
                Arguments.of("Ukraine", "UA", TestsFlagsConstants.UKRAINE),
                Arguments.of("Portugal", "PT", TestsFlagsConstants.PORTUGAL),
                Arguments.of("United States", "US", TestsFlagsConstants.USA),
                Arguments.of(JbstConstants.Strings.UNKNOWN, JbstConstants.Strings.UNKNOWN, TestsFlagsConstants.UNKNOWN),
                Arguments.of(JbstConstants.Strings.UNDEFINED, JbstConstants.Strings.UNDEFINED, TestsFlagsConstants.UNKNOWN)
        );
    }

    @Configuration
    @Import({
            TestConfigurationPropertiesJbstHardcoded.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        private final ResourceLoader resourceLoader;
        private final JbstProperties jbstProperties;

        @Bean
        GeoCountryFlagUtility geoCountryFlagUtility() {
            return new GeoCountryFlagUtilityImpl(
                    this.resourceLoader,
                    this.jbstProperties
            );
        }
    }

    private final GeoCountryFlagUtility componentUnderTest;

    @ParameterizedTest
    @MethodSource("getFlagEmojiTest")
    void getFlagEmojiTest(String country, String countryCode, String expected) {
        // Act
        var actual1 = this.componentUnderTest.getFlagEmojiByCountry(country);
        var actual2 = this.componentUnderTest.getFlagEmojiByCountryCode(countryCode);

        // Assert
        assertThat(actual1).isEqualTo(expected);
        assertThat(actual2).isEqualTo(expected);
    }
}
