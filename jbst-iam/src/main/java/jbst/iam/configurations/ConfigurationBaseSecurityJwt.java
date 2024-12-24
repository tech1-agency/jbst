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
import jbst.iam.handshakes.CsrfInterceptorHandshake;
import jbst.iam.handshakes.SecurityHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import static jbst.foundation.domain.base.AbstractAuthority.*;
import static org.springframework.http.HttpMethod.*;

/**
 * <a href="https://docs.spring.io/spring-security/reference/servlet/integrations/websocket.html">Documentation #1</a>
 * <a href="https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket">Documentation #2</a>
 * <p>
 * Spring Boot 3 Migration Issues 24.04.2024:
 * <p>
 * <a href="https://github.com/spring-projects/spring-security/issues/13640">
 * EnableWebSocketSecurity is not 1:1 replacement for AbstractSecurityWebSocketMessageBrokerConfigurer
 * </a>
 * <p>
 * <a href="https://github.com/jhipster/generator-jhipster/issues/20404">
 * Migrate to Spring Security 6's @EnableWebSocketSecurity (it is not possible to disable CSRF currently)
 * </a>
 */
// idea - reconnect flow: https://stackoverflow.com/questions/53244720/spring-websocket-stomp-exception-handling
@Configuration
@ComponentScan({
        "jbst.iam.crons",
        "jbst.iam.events.publishers.events",
        "jbst.iam.events.publishers.incidents",
        "jbst.iam.events.subscribers.events",
        "jbst.iam.events.subscribers.incidents",
        "jbst.iam.events.subscribers.websockets",
        "jbst.iam.handlers.exceptions",
        "jbst.iam.handshakes",
        "jbst.iam.resources.base",
        "jbst.iam.resources.websockets",
        "jbst.iam.services.base",
        "jbst.iam.tasks.hardware",
        "jbst.iam.template",
        "jbst.iam.tokens",
        "jbst.iam.utils",
        "jbst.iam.validators.base"
})
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
@EnableWebSecurity
@EnableWebSocketMessageBroker
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationBaseSecurityJwt extends AbstractSecurityWebSocketMessageBrokerConfigurer {

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
    // Handshakes
    private final CsrfInterceptorHandshake csrfInterceptorHandshake;
    private final SecurityHandshakeHandler securityHandshakeHandler;
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
            // WARNING: You are asking Spring Security to ignore Ant [pattern='/**/**'].
            // This is not recommended: please use permitAll via HttpSecurity#authorizeHttpRequests instead
            if (this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().isEnabled()) {
                var endpoint = this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().getStompConfigs().getEndpoint();
                web.ignoring().requestMatchers(endpoint + "/**");
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

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/**"))
                .authorizeHttpRequests(authorizeHttpRequests -> {
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
                            .requestMatchers(GET, "/system/csrf").authenticated()
                            .requestMatchers(basePathPrefix + "/test-data/**").authenticated()
                            .requestMatchers(basePathPrefix + "/hardware/**").permitAll()
                            .requestMatchers(basePathPrefix + "/superadmin/**").hasAuthority(SUPERADMIN)
                            .requestMatchers(basePathPrefix + "/**").authenticated();

                    authorizeHttpRequests.requestMatchers("/actuator/**").permitAll();

                    authorizeHttpRequests.anyRequest().authenticated();
        });

        return http.build();
    }

    // =================================================================================================================
    // Websockets
    // =================================================================================================================
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        if (!this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().isEnabled()) {
            return;
        }
        registry.addEndpoint(this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().getStompConfigs().getEndpoint())
                .setAllowedOrigins(this.jbstProperties.getMvcConfigs().getCorsConfigs().getAllowedOrigins())
                .setHandshakeHandler(this.securityHandshakeHandler)
                .addInterceptors(this.csrfInterceptorHandshake)
                .withSockJS();
    }

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry registry) {
        if (!this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().isEnabled()) {
            return;
        }
        registry.anyMessage().authenticated();
    }

    @Override
    public void configureMessageBroker(@NotNull MessageBrokerRegistry registry) {
        if (!this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().isEnabled()) {
            return;
        }
        var broker = this.jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().getBrokerConfigs();
        registry.setApplicationDestinationPrefixes(broker.getApplicationDestinationPrefix());
        registry.enableSimpleBroker(broker.getSimpleDestination());
        registry.setUserDestinationPrefix(broker.getUserDestinationPrefix());
    }

    /**
     * Determines if a CSRF token is required for connecting. This protects against remote
     * sites from connecting to the application and being able to read/write data over the
     * connection. The default is false (the token is required).
     */
    @Override
    protected boolean sameOriginDisabled() {
        return false;
    }
}
