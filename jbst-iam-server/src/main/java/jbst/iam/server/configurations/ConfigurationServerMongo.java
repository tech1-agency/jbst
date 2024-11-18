package jbst.iam.server.configurations;

import jbst.iam.configurations.ConfigurationMongo;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("mongodb")
@Configuration
@Import({
        ConfigurationMongo.class
})
@ComponentScan({
        "jbst.iam.server.mongodb"
})
public class ConfigurationServerMongo {
}
