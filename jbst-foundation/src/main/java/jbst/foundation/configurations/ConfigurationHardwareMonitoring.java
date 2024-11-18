package jbst.foundation.configurations;

import jakarta.annotation.PostConstruct;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.services.hardware.publishers.HardwareMonitoringPublisher;
import jbst.foundation.services.hardware.publishers.impl.HardwareMonitoringPublisherImpl;
import jbst.foundation.services.hardware.resources.HardwareMonitoringResource;
import jbst.foundation.services.hardware.store.HardwareMonitoringStore;
import jbst.foundation.services.hardware.store.impl.HardwareMonitoringStoreImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationHardwareMonitoring {

    // Spring Publisher
    private final ApplicationEventPublisher applicationEventPublisher;
    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getHardwareMonitoringConfigs().assertProperties(new PropertyId("hardwareMonitoringConfigs"));
    }

    @Bean
    HardwareMonitoringStore hardwareMonitoringStore() {
        return new HardwareMonitoringStoreImpl(
                this.jbstProperties
        );
    }

    @Bean
    HardwareMonitoringPublisher hardwareMonitoringPublisher() {
        return new HardwareMonitoringPublisherImpl(
                this.applicationEventPublisher
        );
    }

    @Bean
    HardwareMonitoringResource hardwareMonitoringResource() {
        return new HardwareMonitoringResource(
                this.hardwareMonitoringPublisher()
        );
    }
}
