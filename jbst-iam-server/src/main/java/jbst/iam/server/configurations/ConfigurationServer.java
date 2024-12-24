package jbst.iam.server.configurations;

import jakarta.annotation.PostConstruct;
import jbst.iam.configurations.AbstractJbstSecurityJwtConfigurer;
import jbst.iam.configurations.ConfigurationBaseSecurityJwtWebsockets;
import jbst.iam.server.base.properties.ServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@ComponentScan({
        // -------------------------------------------------------------------------------------------------------------
        "jbst.iam.assistants.current.base",
        "jbst.iam.filters.jwt_extension",
        "jbst.iam.tasks.superadmin",
        // -------------------------------------------------------------------------------------------------------------
        "jbst.iam.server.base"
        // -------------------------------------------------------------------------------------------------------------
})
@Import({
        ConfigurationBaseSecurityJwtWebsockets.class
})
@EnableConfigurationProperties({
        ServerProperties.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationServer implements AbstractJbstSecurityJwtConfigurer {

    // Properties
    private final JbstProperties jbstProperties;
    private final ServerProperties serverProperties;

    @PostConstruct
    public void init() {
        this.serverProperties.getServerConfigs().assertProperties(new PropertyId("serverConfigs"));
    }

    @Override
    public void configure(WebSecurity web) {
        var endpoint = this.jbstProperties.getSecurityJwtWebsocketsConfigs().getStompConfigs().getEndpoint();
        web.ignoring().requestMatchers(endpoint + "/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers(GET, "/system/csrf").authenticated()
                                .requestMatchers("/hardware/**").permitAll()
                );
    }

}
