package jbst.iam.resources.base;

import jbst.foundation.incidents.domain.registration.IncidentRegistration0;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1;
import jbst.iam.configurations.TestRunnerResources1;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.domain.events.EventRegistration0;
import jbst.iam.domain.events.EventRegistration1;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.services.BaseRegistrationService;
import jbst.iam.validators.BaseRegistrationRequestsValidator;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseSecurityRegistrationResourceTest extends TestRunnerResources1 {

    // Services
    private final BaseRegistrationService baseRegistrationService;
    // Publishers
    private final SecurityJwtEventsPublisher securityJwtEventsPublisher;
    private final SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher;
    // Validators
    private final BaseRegistrationRequestsValidator baseRegistrationRequestsValidator;

    // Resource
    private final BaseSecurityRegistrationResource componentUnderTest;

    @BeforeEach
    void beforeEach() {
        this.standaloneSetupByResourceUnderTest(this.componentUnderTest);
        reset(
                this.baseRegistrationService,
                this.securityJwtEventsPublisher,
                this.securityJwtIncidentsPublisher,
                this.baseRegistrationRequestsValidator
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.baseRegistrationService,
                this.securityJwtEventsPublisher,
                this.securityJwtIncidentsPublisher,
                this.baseRegistrationRequestsValidator
        );
    }

    @Test
    void register0() throws Exception {
        // Arrange
        var requestUserRegistration0 = RequestUserRegistration0.hardcoded();

        // Act
        this.mvc.perform(
                        post("/registration/register0")
                                .content(this.objectMapper.writeValueAsString(requestUserRegistration0))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        // Assert
        requestUserRegistration0 = requestUserRegistration0.createReworkedUkraineZoneId();
        verify(this.baseRegistrationRequestsValidator).validateRegistrationRequest0(requestUserRegistration0);
        verify(this.baseRegistrationService).register0(requestUserRegistration0);
        verify(this.securityJwtEventsPublisher).publishRegistration0(new EventRegistration0(requestUserRegistration0));
        verify(this.securityJwtIncidentsPublisher).publishRegistration0(new IncidentRegistration0(requestUserRegistration0.username()));
    }

    @Test
    void register1() throws Exception {
        // Arrange
        var requestUserRegistration1 = RequestUserRegistration1.hardcoded();

        // Act
        this.mvc.perform(
                post("/registration/register1")
                        .content(this.objectMapper.writeValueAsString(requestUserRegistration1))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        // Assert
        requestUserRegistration1 = requestUserRegistration1.createReworkedUkraineZoneId();
        verify(this.baseRegistrationRequestsValidator).validateRegistrationRequest1(requestUserRegistration1);
        verify(this.baseRegistrationService).register1(requestUserRegistration1);
        verify(this.securityJwtEventsPublisher).publishRegistration1(new EventRegistration1(requestUserRegistration1));
        verify(this.securityJwtIncidentsPublisher).publishRegistration1(new IncidentRegistration1(requestUserRegistration1.username()));
    }
}
