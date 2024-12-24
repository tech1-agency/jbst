package jbst.foundation.configurations;

import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.annotation.PostConstruct;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.foundation.incidents.events.publishers.impl.IncidentPublisherImpl;
import jbst.foundation.incidents.events.subscribers.IncidentSubscriber;
import jbst.foundation.incidents.events.subscribers.impl.IncidentSubscriberImpl;
import jbst.foundation.incidents.feigns.clients.IncidentClient;
import jbst.foundation.incidents.feigns.clients.impl.IncidentClientImpl;
import jbst.foundation.incidents.feigns.definitions.IncidentClientDefinition;
import jbst.foundation.incidents.feigns.definitions.IncidentClientDefinitionSlf4j;
import jbst.foundation.incidents.handlers.AsyncUncaughtExceptionHandlerPublisher;
import jbst.foundation.incidents.handlers.ErrorHandlerPublisher;
import jbst.foundation.incidents.handlers.RejectedExecutionHandlerPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;

import static jbst.foundation.utilities.processors.ProcessorsUtility.getNumOfCores;

@Configuration
@EnableConfigurationProperties({
        JbstProperties.class
})
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationIncidents implements AsyncConfigurer {

    // Spring Publisher
    private final ApplicationEventPublisher applicationEventPublisher;
    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getAsyncConfigs().assertProperties(new PropertyId("asyncConfigs"));
        this.jbstProperties.getEventsConfigs().assertProperties(new PropertyId("eventsConfigs"));
        this.jbstProperties.getIncidentConfigs().assertProperties(new PropertyId("incidentConfigs"));
    }

    // ================================================================================================================
    // Incidents: Publisher
    // ================================================================================================================

    @Bean
    IncidentPublisher incidentPublisher() {
        return new IncidentPublisherImpl(
                this.applicationEventPublisher
        );
    }

    // ================================================================================================================
    // Async
    // ================================================================================================================

    @Bean
    RejectedExecutionHandler rejectedExecutionHandler() {
        return new RejectedExecutionHandlerPublisher(
                this.incidentPublisher()
        );
    }

    @Override
    public Executor getAsyncExecutor() {
        var asyncConfigs = this.jbstProperties.getAsyncConfigs();
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(asyncConfigs.getThreadNamePrefix());
        taskExecutor.setCorePoolSize(getNumOfCores(asyncConfigs.asThreadsCorePoolTuplePercentage()));
        taskExecutor.setMaxPoolSize(getNumOfCores(asyncConfigs.asThreadsMaxPoolTuplePercentage()));
        taskExecutor.setRejectedExecutionHandler(this.rejectedExecutionHandler());
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandlerPublisher(
                this.incidentPublisher()
        );
    }

    // ================================================================================================================
    // Events
    // ================================================================================================================

    @Bean
    ErrorHandler errorHandlerPublisher() {
        return new ErrorHandlerPublisher(
                this.incidentPublisher()
        );
    }

    @SuppressWarnings("DuplicatedCode")
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        var eventsConfigs = this.jbstProperties.getEventsConfigs();
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix(eventsConfigs.getThreadNamePrefix());
        taskExecutor.setCorePoolSize(getNumOfCores(eventsConfigs.asThreadsCorePoolTuplePercentage()));
        taskExecutor.setMaxPoolSize(getNumOfCores(eventsConfigs.asThreadsMaxPoolTuplePercentage()));
        taskExecutor.initialize();
        var eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(taskExecutor);
        eventMulticaster.setErrorHandler(this.errorHandlerPublisher());
        return eventMulticaster;
    }

    // ================================================================================================================
    // Incidents
    // ================================================================================================================

    @Bean
    @ConditionalOnProperty(value = "jbst.incident-configs.enabled", havingValue = "true")
    IncidentClientDefinition incidentClientDefinition() {
        var incidentServer = this.jbstProperties.getIncidentConfigs().getRemoteServer();
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(
                        new BasicAuthRequestInterceptor(
                                incidentServer.getCredentials().username().value(),
                                incidentServer.getCredentials().password().value()
                        )
                )
                .target(IncidentClientDefinition.class, incidentServer.getBaseURL());
    }

    @Bean
    @ConditionalOnProperty(value = "jbst.incident-configs.enabled", havingValue = "false", matchIfMissing = true)
    IncidentClientDefinition incidentClientDefinitionSlf4j() {
        return new IncidentClientDefinitionSlf4j();
    }

    @Bean
    IncidentClient incidentClient(IncidentClientDefinition incidentClientDefinition) {
        return new IncidentClientImpl(incidentClientDefinition);
    }

    @Bean
    IncidentSubscriber incidentSubscriber(IncidentClient incidentClient) {
        return new IncidentSubscriberImpl(
                incidentClient
        );
    }
}
