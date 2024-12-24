package jbst.foundation.configurations;

import feign.Feign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.annotation.PostConstruct;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.utilities.browsers.UserAgentDetailsUtility;
import jbst.foundation.utilities.browsers.impl.UserAgentDetailsUtilityImpl;
import jbst.foundation.utilities.geo.facades.GeoCountryFlagUtility;
import jbst.foundation.utilities.geo.facades.GeoLocationFacadeUtility;
import jbst.foundation.utilities.geo.facades.impl.GeoCountryFlagUtilityImpl;
import jbst.foundation.utilities.geo.facades.impl.GeoLocationFacadeUtilityImpl;
import jbst.foundation.utilities.geo.functions.ipapi.feign.IPAPIFeign;
import jbst.foundation.utilities.geo.functions.ipapi.utility.IPAPIGeoLocationUtility;
import jbst.foundation.utilities.geo.functions.ipapi.utility.impl.IPAPIGeoLocationUtilityImpl;
import jbst.foundation.utilities.geo.functions.mindmax.MindMaxGeoLocationUtility;
import jbst.foundation.utilities.geo.functions.mindmax.impl.MindMaxGeoLocationUtilityImpl;
import jbst.foundation.utils.UserMetadataUtils;
import jbst.foundation.utils.impl.UserMetadataUtilsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@EnableConfigurationProperties({
        JbstProperties.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationUtilities {

    // Resources
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final ResourceLoader resourceLoader;
    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getUtilitiesConfigs().assertProperties(new PropertyId("utilitiesConfigs"));
    }

    @Bean
    IPAPIFeign ipapiFeign() {
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .retryer(Retryer.NEVER_RETRY)
                .target(IPAPIFeign.class, "http://ip-api.com");
    }

    @Bean
    UserAgentDetailsUtility userAgentDetailsUtility() {
        return new UserAgentDetailsUtilityImpl(
                this.jbstProperties
        );
    }

    @Bean
    GeoCountryFlagUtility geoCountryFlagUtility() {
        return new GeoCountryFlagUtilityImpl(
                this.resourceLoader,
                this.jbstProperties
        );
    }

    @Bean
    IPAPIGeoLocationUtility ipapiGeoLocationUtility() {
        return new IPAPIGeoLocationUtilityImpl(
                this.ipapiFeign(),
                this.geoCountryFlagUtility()
        );
    }

    @Bean
    MindMaxGeoLocationUtility mindMaxGeoLocationUtility() {
        return new MindMaxGeoLocationUtilityImpl(
                this.resourceLoader,
                this.geoCountryFlagUtility(),
                this.jbstProperties
        );
    }

    @Bean
    GeoLocationFacadeUtility geoLocationFacadeUtility() {
        return new GeoLocationFacadeUtilityImpl(
                this.ipapiGeoLocationUtility(),
                this.mindMaxGeoLocationUtility()
        );
    }

    @Bean
    UserMetadataUtils userMetadataUtils() {
        return new UserMetadataUtilsImpl(
                this.geoLocationFacadeUtility(),
                this.userAgentDetailsUtility()
        );
    }

}
