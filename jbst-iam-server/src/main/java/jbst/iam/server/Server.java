package jbst.iam.server;

import jbst.foundation.domain.constants.JbstConstants;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;
import jbst.iam.essence.AbstractEssenceConstructor;
import jbst.iam.server.configurations.ConfigurationServer;
import jbst.iam.server.configurations.ConfigurationServerMongo;
import jbst.iam.server.configurations.ConfigurationServerPostgres;
import jbst.iam.startup.DefaultStartupEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import static jbst.foundation.domain.enums.Status.COMPLETED;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
@Import({
        ConfigurationServer.class
})
@EnableConfigurationProperties({
        JbstProperties.class
})
@SpringBootApplication(scanBasePackages = {
        "jbst.iam.server.configurations"
})
public class Server extends DefaultStartupEventListener {

    // Publishers
    private final IncidentPublisher incidentPublisher;

    @Autowired
    public Server(
            AbstractEssenceConstructor essenceConstructor,
            JbstProperties jbstProperties,
            IncidentPublisher incidentPublisher
    ) {
        super(essenceConstructor, jbstProperties);
        this.incidentPublisher = incidentPublisher;
    }

    public static void main(String[] args) {
        var springApplication = new SpringApplication(Server.class);
        var applicationContext = springApplication.run(args);
        var jbstProperties = applicationContext.getBean(JbstProperties.class);
        LOGGER.info(JbstConstants.Logs.getServerContainer(jbstProperties.getServerConfigs(), COMPLETED));
    }

    @Override
    public void onStartup() {
        try {
            super.onStartup();
            LOGGER.info(JbstConstants.Logs.getServerStartup(this.jbstProperties.getServerConfigs(), COMPLETED));
        } catch (RuntimeException ex) {
            this.incidentPublisher.publishThrowable(ex);
        }
    }
}
