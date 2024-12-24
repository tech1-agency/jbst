package jbst.hardware.monitoring.server.configurations;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.annotation.PostConstruct;
import jbst.foundation.configurations.ConfigurationAsync;
import jbst.foundation.configurations.ConfigurationEvents;
import jbst.foundation.configurations.ConfigurationSpringBootServer;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.hardware.monitoring.server.client.HardwareMonitoringClientDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableConfigurationProperties({
        JbstProperties.class
})
@Import({
        ConfigurationAsync.class,
        ConfigurationEvents.class,
        ConfigurationSpringBootServer.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationServer {

    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getHardwareServerConfigs().printProperties(new PropertyId("hardwareServerConfigs"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated()
        ).csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public HardwareMonitoringClientDefinition hardwareMonitoringClientDefinition() {
        var hardwareServerConfigs = this.jbstProperties.getHardwareServerConfigs();
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(HardwareMonitoringClientDefinition.class, hardwareServerConfigs.getBaseURL());
    }
}
