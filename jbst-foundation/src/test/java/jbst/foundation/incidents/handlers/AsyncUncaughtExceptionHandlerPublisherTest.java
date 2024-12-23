package jbst.foundation.incidents.handlers;

import jbst.foundation.incidents.domain.Incident;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Arrays;

import static jbst.foundation.utilities.random.RandomUtility.*;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AsyncUncaughtExceptionHandlerPublisherTest {

    @Configuration
    static class ContextConfiguration {
        @Bean
        IncidentPublisher incidentPublisher() {
            return mock(IncidentPublisher.class);
        }

        @Bean
        AsyncUncaughtExceptionHandlerPublisher asyncUncaughtExceptionHandlerPublisher() {
            return new AsyncUncaughtExceptionHandlerPublisher(
                    this.incidentPublisher()
            );
        }
    }

    // Publisher
    private final IncidentPublisher incidentPublisher;

    private final AsyncUncaughtExceptionHandlerPublisher componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.incidentPublisher
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.incidentPublisher
        );
    }

    @Test
    void handleUncaughtExceptionTest() {
        // Arrange
        var throwable = mock(Throwable.class);
        var method = randomMethod();
        var params = new Object[] { randomString(), randomLong() };

        // Act
        this.componentUnderTest.handleUncaughtException(throwable, method, params);

        // Assert
        verify(this.incidentPublisher).publishIncident(new Incident(throwable, method, Arrays.asList(params)));
    }
}
