package jbst.iam.resources.base;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jbst.foundation.incidents.domain.registration.IncidentRegistration0;
import jbst.iam.annotations.AbstractJbstBaseSecurityResource;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.domain.events.EventRegistration0;
import jbst.iam.domain.events.EventRegistration1;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.services.BaseRegistrationService;
import jbst.iam.validators.BaseRegistrationRequestsValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jbst.foundation.domain.exceptions.authentication.RegistrationException;
import jbst.foundation.incidents.domain.registration.IncidentRegistration1;

// Swagger
@Tag(name = "[jbst] Registration API")
// Spring
@Slf4j
@AbstractJbstBaseSecurityResource
@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseSecurityRegistrationResource {

    // Services
    private final BaseRegistrationService baseRegistrationService;
    // Publishers
    private final SecurityJwtEventsPublisher securityJwtEventsPublisher;
    private final SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher;
    // Validators
    private final BaseRegistrationRequestsValidator baseRegistrationRequestsValidator;

    @PostMapping("/register0")
    @ResponseStatus(HttpStatus.OK)
    public void register0(@RequestBody @Valid RequestUserRegistration0 request) throws RegistrationException {
        request = request.createReworkedUkraineZoneId();
        this.baseRegistrationRequestsValidator.validateRegistrationRequest0(request);
        this.baseRegistrationService.register0(request);
        this.securityJwtEventsPublisher.publishRegistration0(new EventRegistration0(request));
        this.securityJwtIncidentsPublisher.publishRegistration0(new IncidentRegistration0(request.username()));
    }

    @PostMapping("/register1")
    @ResponseStatus(HttpStatus.OK)
    public void register1(@RequestBody @Valid RequestUserRegistration1 request) throws RegistrationException {
        request = request.createReworkedUkraineZoneId();
        this.baseRegistrationRequestsValidator.validateRegistrationRequest1(request);
        this.baseRegistrationService.register1(request);
        this.securityJwtEventsPublisher.publishRegistration1(new EventRegistration1(request));
        this.securityJwtIncidentsPublisher.publishRegistration1(new IncidentRegistration1(request.username()));
    }
}
