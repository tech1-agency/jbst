package jbst.iam.events.subscribers.websockets;

import jbst.iam.tasks.hardware.HardwareBackPressureTimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jbst.foundation.domain.events.hardware.EventLastHardwareMonitoringDatapoint;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.foundation.services.hardware.store.HardwareMonitoringStore;
import jbst.foundation.services.hardware.subscribers.base.BaseHardwareMonitoringSubscriber;

@Slf4j
@Component
public class HardwareMonitoringSubscriberWebsockets extends BaseHardwareMonitoringSubscriber {

    // TimerTasks
    private final HardwareBackPressureTimerTask hardwareBackPressureTimerTask;
    // Incidents
    private final IncidentPublisher incidentPublisher;

    public HardwareMonitoringSubscriberWebsockets(
            HardwareMonitoringStore hardwareMonitoringStore,
            HardwareBackPressureTimerTask hardwareBackPressureTimerTask,
            IncidentPublisher incidentPublisher
    ) {
        super(
                hardwareMonitoringStore
        );
        this.hardwareBackPressureTimerTask = hardwareBackPressureTimerTask;
        this.incidentPublisher = incidentPublisher;
    }

    @Override
    public void onLastHardwareMonitoringDatapoint(EventLastHardwareMonitoringDatapoint event) {
        try {
            super.onLastHardwareMonitoringDatapoint(event);
            if (this.hardwareBackPressureTimerTask.isAnyProblemOrFirstDatapoint()) {
                this.hardwareBackPressureTimerTask.send();
            }
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }
}
