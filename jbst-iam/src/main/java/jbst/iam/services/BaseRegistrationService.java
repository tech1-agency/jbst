package jbst.iam.services;

import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;

public interface BaseRegistrationService {
    void register0(RequestUserRegistration0 requestUserRegistration0);
    void register1(RequestUserRegistration1 requestUserRegistration1);
}
