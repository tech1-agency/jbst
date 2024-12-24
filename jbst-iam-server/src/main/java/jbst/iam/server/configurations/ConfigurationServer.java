package jbst.iam.server.configurations;

import jbst.foundation.domain.properties.JbstProperties;
import jbst.iam.configurations.AbstractJbstSecurityJwtConfigurer;
import jbst.iam.configurations.ConfigurationBaseSecurityJwt;
import jbst.iam.configurations.ConfigurationBaseSecurityJwtFallbackBases;
import jbst.iam.server.base.properties.ServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@Import({
        ConfigurationBaseSecurityJwt.class,
        ConfigurationBaseSecurityJwtFallbackBases.class,
        ConfigurationServerBase.class
})
@EnableConfigurationProperties({
        ServerProperties.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationServer implements AbstractJbstSecurityJwtConfigurer {

    // Properties
    private final JbstProperties jbstProperties;

    @Override
    public void configure(WebSecurity web) {
        var endpoint = this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().getStompConfigs().getEndpoint();
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
