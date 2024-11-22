package jbst.iam.startup;

import jbst.foundation.domain.constants.JbstConstants;
import jbst.foundation.domain.enums.Status;
import jbst.foundation.domain.enums.Toggle;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.iam.essence.AbstractEssenceConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DefaultStartupEventListener implements BaseStartupEventListener {
    private static final String STARTUP_MESSAGE = JbstConstants.Logs.PREFIX + " Startup event listener. Status: {}";

    // Essence
    protected final AbstractEssenceConstructor essenceConstructor;
    // Properties
    protected final JbstProperties jbstProperties;

    @Override
    public void onStartup() {
        LOGGER.info(JbstConstants.Symbols.LINE_SEPARATOR_INTERPUNCT);
        LOGGER.info(STARTUP_MESSAGE, Status.STARTED.formatAnsi());

        var defaultUsers = this.jbstProperties.getSecurityJwtConfigs().getEssenceConfigs().getDefaultUsers();
        LOGGER.info("{} Essence feature 'default-users' — {}", JbstConstants.Logs.PREFIX, Toggle.of(defaultUsers.isEnabled()));
        if (defaultUsers.isEnabled()) {
            this.essenceConstructor.addDefaultUsers();
        }

        var invitations = this.jbstProperties.getSecurityJwtConfigs().getEssenceConfigs().getInvitations();
        LOGGER.info("{} Essence feature 'invitations' — {}", JbstConstants.Logs.PREFIX, Toggle.of(invitations.isEnabled()));
        if (invitations.isEnabled()) {
            this.essenceConstructor.addDefaultUsersInvitations();
        }

        LOGGER.info(STARTUP_MESSAGE, Status.COMPLETED.formatAnsi());
        LOGGER.info(JbstConstants.Symbols.LINE_SEPARATOR_INTERPUNCT);
    }
}
