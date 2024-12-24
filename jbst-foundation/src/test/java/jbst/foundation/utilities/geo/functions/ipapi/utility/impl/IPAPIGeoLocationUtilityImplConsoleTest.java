package jbst.foundation.utilities.geo.functions.ipapi.utility.impl;

import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;
import jbst.foundation.configurations.ConfigurationUtilities;
import jbst.foundation.domain.exceptions.geo.GeoLocationNotFoundException;
import jbst.foundation.domain.http.requests.IPAddress;
import jbst.foundation.utilities.geo.functions.ipapi.utility.IPAPIGeoLocationUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@Slf4j
@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class IPAPIGeoLocationUtilityImplConsoleTest {

    @Configuration
    @Import({
            ConfigurationUtilities.class,
            TestConfigurationPropertiesJbstHardcoded.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {

    }

    private final IPAPIGeoLocationUtility componentUnderTest;

    @Test
    @Disabled
    void australiaTest() throws GeoLocationNotFoundException {
        // Act
        var geoLocation = this.componentUnderTest.getGeoLocation(new IPAddress("1.1.1.1"));

        // Assert
        LOGGER.debug("geoLocation: " + geoLocation);
    }

    @Test
    @Disabled
    void localhostTest() throws GeoLocationNotFoundException {
        // Act
        var geoLocation = this.componentUnderTest.getGeoLocation(new IPAddress("127.0.0.1"));

        // Assert
        LOGGER.debug("geoLocation: " + geoLocation);
    }
}
