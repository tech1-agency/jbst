package jbst.iam.server.base.startup;

import jbst.iam.essence.AbstractEssenceConstructor;
import jbst.iam.startup.DefaultStartupEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jbst.foundation.domain.properties.ApplicationFrameworkProperties;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;

import static jbst.foundation.domain.constants.LogsConstants.SERVER_STARTUP_LISTENER_1;
import static jbst.foundation.domain.enums.Status.COMPLETED;

@Slf4j
@Service
public class StartupEventListener extends DefaultStartupEventListener {

    // Publishers
    private final IncidentPublisher incidentPublisher;

    @Autowired
    public StartupEventListener(
            AbstractEssenceConstructor essenceConstructor,
            IncidentPublisher incidentPublisher,
            ApplicationFrameworkProperties applicationFrameworkProperties
    ) {
        super(
                essenceConstructor,
                applicationFrameworkProperties
        );
        this.incidentPublisher = incidentPublisher;
    }

    @Override
    public void onStartup() {
        try {
            super.onStartup();
            var serverConfigs = this.applicationFrameworkProperties.getServerConfigs();
            var mavenDetails = this.applicationFrameworkProperties.getMavenConfigs().asMavenDetails();
            LOGGER.info(SERVER_STARTUP_LISTENER_1, serverConfigs.getName(), mavenDetails.version(), COMPLETED);
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }
}