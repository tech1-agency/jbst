package jbst.iam.tasks.superadmin;

import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.template.WssMessagingTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.system.reset_server.ResetServerStatus;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;

import java.util.Set;

import static jbst.foundation.domain.constants.FrameworkLogsConstants.SERVER_RESET_SERVER_TASK;
import static jbst.foundation.domain.enums.Status.FAILURE;
import static jbst.foundation.utilities.concurrent.SleepUtility.sleepMilliseconds;

@Slf4j
@Getter
@Component
public class TestDataResetServerTask extends AbstractSuperAdminResetServerTask {

    // Wss
    private final WssMessagingTemplate wssMessagingTemplate;

    private final ResetServerStatus status = new ResetServerStatus(6);

    @Autowired
    public TestDataResetServerTask(
            IncidentPublisher incidentPublisher,
            WssMessagingTemplate wssMessagingTemplate
    ) {
        super(
                incidentPublisher
        );
        this.wssMessagingTemplate = wssMessagingTemplate;
    }

    @Override
    public void resetOnServer(JwtUser initiator) {
        var username = initiator.username();
        var usernames = Set.of(username);
        try {
            this.status.reset();

            this.computeAndSendResetServerProgress(usernames, "[Server] Test Data Stage #1");
            this.computeAndSendResetServerProgress(usernames, "[Server] Test Data Stage #2");
            this.computeAndSendResetServerProgress(usernames, "[Server] Test Data Stage #3");
            this.computeAndSendResetServerProgress(usernames, "[Server] Test Data Stage #4");
            this.computeAndSendResetServerProgress(usernames, "[Server] Test Data Stage #5");
            this.computeAndSendResetServerProgress(usernames, "[Server] Test Data Stage #6");

            this.status.complete(initiator.zoneId());
            this.wssMessagingTemplate.sendResetServerStatus(usernames, this.status);
        } catch (RuntimeException ex) {
            // WARNING: any exceptions should NOT be expected behaviour, method required ASAP fix
            this.status.setFailureDescription(ex);
            this.wssMessagingTemplate.sendResetServerStatus(usernames, this.status);
            LOGGER.error(SERVER_RESET_SERVER_TASK, username, FAILURE);
            this.incidentPublisher.publishThrowable(ex);
        }
    }

    private void computeAndSendResetServerProgress(Set<Username> usernames, String description) {
        this.status.nextStage(description);
        this.wssMessagingTemplate.sendResetServerStatus(usernames, this.status);
        LOGGER.info(SERVER_RESET_SERVER_TASK, description, this.status.getPercentage().percentage() + "%");
        sleepMilliseconds(1000);
    }
}
