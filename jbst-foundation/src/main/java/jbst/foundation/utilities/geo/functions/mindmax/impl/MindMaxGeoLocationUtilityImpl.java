package jbst.foundation.utilities.geo.functions.mindmax.impl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import jbst.foundation.domain.enums.Toggle;
import jbst.foundation.domain.geo.GeoLocation;
import jbst.foundation.domain.http.requests.IPAddress;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.utilities.geo.facades.GeoCountryFlagUtility;
import jbst.foundation.utilities.geo.functions.mindmax.MindMaxGeoLocationUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.InetAddress;

import static jbst.foundation.domain.constants.JbstConstants.Logs.PREFIX_UTILITIES;
import static jbst.foundation.domain.enums.Status.FAILURE;
import static jbst.foundation.domain.enums.Status.SUCCESS;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.contactDevelopmentTeam;

@Slf4j
public class MindMaxGeoLocationUtilityImpl implements MindMaxGeoLocationUtility {
    private static final String GEO_DATABASE_NAME = "GeoLite2-City.mmdb";

    // Database
    private final DatabaseReader databaseReader;
    // Utilities
    private final GeoCountryFlagUtility geoCountryFlagUtility;
    // Properties
    private final JbstProperties jbstProperties;

    public MindMaxGeoLocationUtilityImpl(
            ResourceLoader resourceLoader,
            GeoCountryFlagUtility geoCountryFlagUtility,
            JbstProperties jbstProperties
    ) {
        this.geoCountryFlagUtility = geoCountryFlagUtility;
        this.jbstProperties = jbstProperties;
        var geoLocationsConfigs = jbstProperties.getUtilitiesConfigs().getGeoLocationsConfigs();
        LOGGER.info("{} Geo location {} database — {}", PREFIX_UTILITIES, GEO_DATABASE_NAME, Toggle.of(geoLocationsConfigs.isGeoLiteCityDatabaseEnabled()));
        if (geoLocationsConfigs.isGeoLiteCityDatabaseEnabled()) {
            try {
                var resource = resourceLoader.getResource("classpath:" + GEO_DATABASE_NAME);
                var inputStream = resource.getInputStream();
                this.databaseReader = new DatabaseReader.Builder(inputStream).build();
                LOGGER.info("{} Geo location {} database configuration status: {}", PREFIX_UTILITIES, GEO_DATABASE_NAME, SUCCESS);
            } catch (IOException | RuntimeException ex) {
                LOGGER.error("{} Geo location {} database configuration status: {}", PREFIX_UTILITIES, GEO_DATABASE_NAME, FAILURE);
                LOGGER.error("Please make sure {} database is in classpath", GEO_DATABASE_NAME);
                throw new IllegalArgumentException(ex.getMessage());
            }
        } else {
            this.databaseReader = null;
        }
    }

    @Override
    public GeoLocation getGeoLocation(IPAddress ipAddress) {
        if (!this.jbstProperties.getUtilitiesConfigs().getGeoLocationsConfigs().isGeoLiteCityDatabaseEnabled()) {
            return GeoLocation.unknown(ipAddress, contactDevelopmentTeam("Geo configurations failure"));
        }
        try {
            var inetAddress = InetAddress.getByName(ipAddress.value());
            var response = this.databaseReader.city(inetAddress);
            var countryCode = response.getCountry().getIsoCode();
            var countryFlag = this.geoCountryFlagUtility.getFlagEmojiByCountryCode(countryCode);
            return GeoLocation.processed(
                    ipAddress,
                    response.getCountry().getName(),
                    countryCode,
                    countryFlag,
                    response.getCity().getName()
            );
        } catch (IOException | GeoIp2Exception ex) {
            return GeoLocation.unknown(ipAddress, ex.getMessage());
        }
    }
}
