package jbst.iam.template.impl;

import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.configs.SecurityJwtConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.WebsocketsConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.CsrfConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.MessageBrokerRegistryConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.StompEndpointRegistryConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.websockets.WebsocketsFeaturesConfigs;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.iam.domain.events.WebsocketEvent;
import jbst.iam.template.WssMessagingTemplate;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.stream.Stream;

import static jbst.foundation.utilities.random.RandomUtility.randomString;
import static org.mockito.Mockito.*;

// TODO [YYL] fixme
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class WssMessagingTemplateImplTest {

    private static Stream<Arguments> convertAndSendToUserTestArgs() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(false, false)
        );
    }

    @Configuration
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        @Bean
        JbstProperties jbstProperties() {
            return mock(JbstProperties.class);
        }

        @Bean
        SimpMessagingTemplate simpMessagingTemplate() {
            return mock(SimpMessagingTemplate.class);
        }

        @Bean
        IncidentPublisher serverIncidentPublisher() {
            return mock(IncidentPublisher.class);
        }

        @Bean
        WssMessagingTemplate wssMessagingTemplate() {
            return new WssMessagingTemplateImpl(
                    this.simpMessagingTemplate(),
                    this.serverIncidentPublisher(),
                    this.jbstProperties()
            );
        }
    }

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final IncidentPublisher incidentPublisher;
    private final JbstProperties jbstProperties;

    private final WssMessagingTemplate componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.simpMessagingTemplate,
                this.incidentPublisher,
                this.jbstProperties
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.simpMessagingTemplate,
                this.incidentPublisher,
                this.jbstProperties
        );
    }

    @Test
    void convertAndSendToUserThrowExceptionTest() {
        // Assert
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.hardcoded());
        var username = Username.random();
        var websocketEvent = mock(WebsocketEvent.class);
        var ex = new MessagingException(randomString());
        var destination = "/" + randomString();
        doThrow(ex).when(this.simpMessagingTemplate).convertAndSendToUser(username.value(), "/queue" + destination, websocketEvent);

        // Act
        this.componentUnderTest.sendEventToUser(username, destination, websocketEvent);

        // Assert
        verify(this.jbstProperties, times(2)).getSecurityJwtConfigs();
        verify(this.simpMessagingTemplate).convertAndSendToUser(username.value(), "/queue" + destination, websocketEvent);
        verify(this.incidentPublisher).publishThrowable(ex);
        verifyNoMoreInteractions(this.simpMessagingTemplate);
    }

    @ParameterizedTest
    @MethodSource("convertAndSendToUserTestArgs")
    void convertAndSendToUserTest(boolean configs, boolean expectedSend) {
        // Assert
        var securityJwtConfigs = mock(SecurityJwtConfigs.class);
        when(securityJwtConfigs.getWebsocketsConfigs()).thenReturn(
                new WebsocketsConfigs(
                        configs,
                        CsrfConfigs.hardcoded(),
                        StompEndpointRegistryConfigs.hardcoded(),
                        MessageBrokerRegistryConfigs.hardcoded(),
                        WebsocketsFeaturesConfigs.hardcoded()
                )
        );
        var username = Username.random();
        var destination = randomString();
        var websocketEvent = mock(WebsocketEvent.class);

        // Act
        this.componentUnderTest.sendEventToUser(username, destination, websocketEvent);

        // Assert
        if (expectedSend) {
            verify(this.jbstProperties, times(2)).getSecurityJwtConfigs();
            verify(this.simpMessagingTemplate).convertAndSendToUser(username.value(), "/queue" + destination, websocketEvent);
            verifyNoMoreInteractions(this.simpMessagingTemplate);
        } else {
            verify(this.jbstProperties).getSecurityJwtConfigs();
        }
    }
}
