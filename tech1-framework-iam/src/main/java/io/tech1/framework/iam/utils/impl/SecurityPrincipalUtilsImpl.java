package io.tech1.framework.iam.utils.impl;

import io.tech1.framework.iam.domain.jwt.JwtUser;
import io.tech1.framework.iam.utils.SecurityPrincipalUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityPrincipalUtilsImpl implements SecurityPrincipalUtils {

    @Override
    public JwtUser getAuthenticatedJwtUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (nonNull(authentication)) {
            try {
                return (JwtUser) authentication.getPrincipal();
            } catch (ClassCastException ex) {
                var message = "Illegal request. Authentication principal is not a JwtUser. EX: " + ex.getMessage();
                throw new IllegalArgumentException(message);
            }
        } else {
            var message = "Illegal request. Authentication is null";
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public String getAuthenticatedUsername() {
        return this.getAuthenticatedJwtUser().getUsername();
    }

    @Override
    public String getAuthenticatedUsernameOrUnexpected() {
        try {
            return this.getAuthenticatedJwtUser().getUsername();
        } catch (RuntimeException ex) {
            return "[unexpected]";
        }
    }
}
