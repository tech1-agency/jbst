package jbst.iam.assistants.current.base;

import jakarta.servlet.http.HttpServletRequest;
import jbst.iam.assistants.current.CurrentSessionAssistant;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.dto.responses.ResponseUserSessionsTable;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.security.CurrentClientUser;
import jbst.iam.repositories.UsersSessionsRepository;
import jbst.iam.sessions.SessionRegistry;
import jbst.iam.tokens.facade.TokensProvider;
import jbst.iam.utils.SecurityPrincipalUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.exceptions.tokens.AccessTokenNotFoundException;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.services.hardware.store.HardwareMonitoringStore;

import java.util.HashMap;
import java.util.Set;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseCurrentSessionAssistant implements CurrentSessionAssistant {
    private static final String HARDWARE = "hardware";

    // Sessions
    protected final SessionRegistry sessionRegistry;
    // Repositories
    protected final UsersSessionsRepository usersSessionsRepository;
    // Stores
    protected final HardwareMonitoringStore hardwareMonitoringStore;
    // Tokens
    protected final TokensProvider tokensProvider;
    // Utilities
    protected final SecurityPrincipalUtils securityPrincipalUtils;
    // Properties
    protected final JbstProperties jbstProperties;

    @Override
    public Username getCurrentUsername() {
        return Username.of(this.securityPrincipalUtils.getAuthenticatedUsername());
    }

    @Override
    public JwtUser getCurrentJwtUser() {
        return this.securityPrincipalUtils.getAuthenticatedJwtUser();
    }

    @Override
    public CurrentClientUser getCurrentClientUser() {
        var user = this.getCurrentJwtUser();

        var attributes = nonNull(user.attributes()) ? user.attributes() : new HashMap<String, Object>();
        if (this.jbstProperties.getHardwareMonitoringConfigs().isEnabled()) {
            attributes.put(HARDWARE, this.hardwareMonitoringStore.getHardwareMonitoringWidget());
        }

        return new CurrentClientUser(
                user.username(),
                user.email(),
                user.name(),
                user.zoneId(),
                user.passwordChangeRequired(),
                user.emailConfirmationRequired(),
                user.authorities(),
                attributes
        );
    }

    @Override
    public UserSession getCurrentUserSession(HttpServletRequest httpServletRequest) throws AccessTokenNotFoundException {
        var cookie = this.tokensProvider.readRequestAccessToken(httpServletRequest);
        return this.usersSessionsRepository.isPresent(JwtAccessToken.of(cookie.value())).value();
    }

    @Override
    public ResponseUserSessionsTable getCurrentUserDbSessionsTable(RequestAccessToken requestAccessToken) {
        var username = this.getCurrentUsername();
        this.sessionRegistry.cleanByExpiredRefreshTokens(Set.of(username));
        return this.sessionRegistry.getSessionsTable(username, requestAccessToken);
    }
}
