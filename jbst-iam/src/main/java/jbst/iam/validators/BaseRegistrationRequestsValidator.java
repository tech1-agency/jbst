package jbst.iam.validators;

import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.foundation.domain.exceptions.authentication.RegistrationException;

public interface BaseRegistrationRequestsValidator {
    void validateRegistrationRequest0(RequestUserRegistration0 request) throws RegistrationException;
    void validateRegistrationRequest1(RequestUserRegistration1 request) throws RegistrationException;
}
