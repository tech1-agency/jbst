package jbst.iam.domain.events;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Username;

public record EventRegistration0Failure(
        Email email,
        Username username,
        String exception
) {
}
