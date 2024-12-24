package jbst.iam.tasks.hardware;

import jbst.foundation.domain.concurrent.AbstractInfiniteTimerTask;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.time.SchedulerConfiguration;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.foundation.services.hardware.store.HardwareMonitoringStore;
import jbst.iam.sessions.SessionRegistry;
import jbst.iam.template.WssMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class HardwareBackPressureTimerTask extends AbstractInfiniteTimerTask {

    // Sessions
    private final SessionRegistry sessionRegistry;
    // Wss
    private final WssMessagingTemplate wssMessagingTemplate;
    // Store
    private final HardwareMonitoringStore hardwareMonitoringStore;
    // Publishers
    private final IncidentPublisher incidentPublisher;

    // 60L seconds -> consider add to user settings but on 25.11.2022 no reason to add
    public HardwareBackPressureTimerTask(
            SessionRegistry sessionRegistry,
            WssMessagingTemplate wssMessagingTemplate,
            HardwareMonitoringStore hardwareMonitoringStore,
            IncidentPublisher incidentPublisher,
            JbstProperties jbstProperties
    ) {
        super(
                new SchedulerConfiguration(60L, 60L, TimeUnit.SECONDS)
        );
        this.sessionRegistry = sessionRegistry;
        this.wssMessagingTemplate = wssMessagingTemplate;
        this.hardwareMonitoringStore = hardwareMonitoringStore;
        this.incidentPublisher = incidentPublisher;
        if (jbstProperties.getSecurityJwtConfigs().getWebsocketsConfigs().getFeaturesConfigs().getHardwareConfigs().isEnabled()) {
            this.start();
        }
    }

    @Override
    public void onTick() {
        try {
            this.send();
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }

    public void send() {
        var usernames = this.sessionRegistry.getActiveSessionsUsernames();
        this.wssMessagingTemplate.sendHardwareMonitoring(usernames, this.hardwareMonitoringStore.getHardwareMonitoringWidget().datapoint());
    }

    public boolean isAnyProblemOrFirstDatapoint() {
        return this.hardwareMonitoringStore.containsOneElement() || this.hardwareMonitoringStore.getHardwareMonitoringWidget().datapoint().isAnyProblem();
    }
}
