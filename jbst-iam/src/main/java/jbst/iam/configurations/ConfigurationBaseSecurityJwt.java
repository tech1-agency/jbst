package jbst.iam.configurations;

import jakarta.annotation.PostConstruct;
import jbst.foundation.configurations.*;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.constants.JbstConstants;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.iam.assistants.userdetails.JwtUserDetailsService;
import jbst.iam.filters.jwt.JwtTokensFilter;
import jbst.iam.handlers.exceptions.JwtAccessDeniedExceptionHandler;
import jbst.iam.handlers.exceptions.JwtAuthenticationEntryPointExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static jbst.foundation.domain.base.AbstractAuthority.*;
import static org.springframework.http.HttpMethod.*;

@Configuration
@ComponentScan({
        "jbst.iam.crons",
        "jbst.iam.events.publishers.base",
        "jbst.iam.events.publishers.impl",
        "jbst.iam.events.subscribers.base",
        "jbst.iam.events.subscribers.impl",
        "jbst.iam.handlers.exceptions",
        "jbst.iam.resources.base",
        "jbst.iam.services.base",
        "jbst.iam.tokens",
        "jbst.iam.utils",
        "jbst.iam.validators.base"
})
@EnableWebSecurity
@Import({
        ConfigurationJasypt.class,
        ConfigurationHardwareMonitoring.class,
        ConfigurationUtilities.class,
        ConfigurationSpringBootServer.class,
        ConfigurationBaseSecurityJwtWebMVC.class,
        ConfigurationBaseSecurityJwtFilters.class,
        ConfigurationBaseSecurityJwtPasswords.class,
        ConfigurationIncidents.class,
        ConfigurationEmails.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationBaseSecurityJwt {

    // Assistants
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JwtUserDetailsService jwtUserDetailsService;
    // Passwords
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    // Filters
    private final JwtTokensFilter jwtTokensFilter;
    // Handlers
    private final JwtAuthenticationEntryPointExceptionHandler jwtAuthenticationEntryPointExceptionHandler;
    private final JwtAccessDeniedExceptionHandler jwtAccessDeniedExceptionHandler;
    // Configurer
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final AbstractJbstSecurityJwtConfigurer abstractJbstSecurityJwtConfigurer;
    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getSecurityJwtConfigs().assertProperties(new PropertyId("securityJwtConfigs"));
    }

    @Autowired
    void configureAuthenticationManager(AuthenticationManagerBuilder builder) throws Exception {
        builder
                .userDetailsService(this.jwtUserDetailsService)
                .passwordEncoder(this.bCryptPasswordEncoder);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            if (this.jbstProperties.getServerConfigs().isSpringdocEnabled()) {
                web.ignoring().requestMatchers(JbstConstants.Swagger.ENDPOINTS.toArray(new String[0]));
            }
            this.abstractJbstSecurityJwtConfigurer.configure(web);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var basePathPrefix = this.jbstProperties.getMvcConfigs().getBasePathPrefix();

        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        this.jwtTokensFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(this.jwtAuthenticationEntryPointExceptionHandler)
                                .accessDeniedHandler(this.jwtAccessDeniedExceptionHandler)
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // WARNING: order is important, configurer must have possibility to override matchers below
        this.abstractJbstSecurityJwtConfigurer.configure(http);

        http.authorizeHttpRequests(authorizeHttpRequests -> {
            authorizeHttpRequests
                    .requestMatchers(POST, basePathPrefix + "/authentication/login").permitAll()
                    .requestMatchers(POST, basePathPrefix + "/authentication/logout").permitAll()
                    .requestMatchers(POST, basePathPrefix + "/authentication/refreshToken").permitAll()
                    .requestMatchers(GET, basePathPrefix + "/session/current").authenticated()
                    .requestMatchers(POST, basePathPrefix + "/registration/register0").anonymous()
                    .requestMatchers(POST, basePathPrefix + "/registration/register1").anonymous()
                    .requestMatchers(POST, basePathPrefix + "/user/update1").authenticated()
                    .requestMatchers(POST, basePathPrefix + "/user/update2").authenticated()
                    .requestMatchers(POST, basePathPrefix + "/user/changePassword1").authenticated()
                    .requestMatchers(GET, basePathPrefix + "/tokens/email/confirm").permitAll()
                    .requestMatchers(basePathPrefix + "/tokens/password/reset").anonymous();

            if (this.jbstProperties.getSecurityJwtConfigs().getEssenceConfigs().getInvitations().isEnabled()) {
                authorizeHttpRequests
                        .requestMatchers(GET, basePathPrefix + "/invitations").hasAuthority(INVITATIONS_READ)
                        .requestMatchers(POST, basePathPrefix + "/invitations").hasAuthority(INVITATIONS_WRITE)
                        .requestMatchers(DELETE, basePathPrefix + "/invitations/{invitationId}").hasAuthority(INVITATIONS_WRITE);
            } else {
                authorizeHttpRequests.requestMatchers(basePathPrefix + "/invitations/**").denyAll();
            }

            authorizeHttpRequests
                    .requestMatchers(basePathPrefix + "/test-data/**").authenticated()
                    .requestMatchers(basePathPrefix + "/hardware/**").authenticated()
                    .requestMatchers(basePathPrefix + "/superadmin/**").hasAuthority(SUPERADMIN)
                    .requestMatchers(basePathPrefix + "/**").authenticated();

            authorizeHttpRequests.requestMatchers("/actuator/**").permitAll();

            authorizeHttpRequests.anyRequest().authenticated();
        });

        return http.build();
    }
}
