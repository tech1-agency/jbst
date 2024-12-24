package jbst.iam.configurations;

import jakarta.annotation.PostConstruct;
import jbst.iam.assistants.userdetails.MongoUserDetailsAssistant;
import jbst.iam.essence.MongoBaseEssenceConstructor;
import jbst.iam.events.publishers.incidents.SecurityJwtIncidentsPublisher;
import jbst.iam.events.publishers.events.SecurityJwtEventsPublisher;
import jbst.iam.repositories.mongodb.MongoInvitationsRepository;
import jbst.iam.repositories.mongodb.MongoUsersRepository;
import jbst.iam.repositories.mongodb.MongoUsersSessionsRepository;
import jbst.iam.services.mongodb.MongoBaseUsersSessionsService;
import jbst.iam.sessions.MongoSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;

@Configuration
@EnableConfigurationProperties({
        JbstProperties.class
})
@ComponentScan({
        "jbst.iam.services.mongodb",
        "jbst.iam.validators.mongodb",
})
@Import({
        ConfigurationMongoRepositories.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationMongo {

    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getMongodbSecurityJwtConfigs().assertProperties(new PropertyId("mongodbSecurityJwtConfigs"));
    }

    @Bean
    MongoUserDetailsAssistant mongoUserDetailsAssistant(
            MongoUsersRepository mongoUsersRepository
    ) {
        return new MongoUserDetailsAssistant(
                mongoUsersRepository
        );
    }

    @Bean
    MongoBaseEssenceConstructor mongoBaseEssenceConstructor(
            MongoInvitationsRepository mongoInvitationsRepository,
            MongoUsersRepository mongoUsersRepository,
            JbstProperties jbstProperties
    ) {
        return new MongoBaseEssenceConstructor(
                mongoInvitationsRepository,
                mongoUsersRepository,
                jbstProperties
        );
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    MongoSessionRegistry mongoSessionRegistry(
            SecurityJwtEventsPublisher securityJwtEventsPublisher,
            SecurityJwtIncidentsPublisher securityJwtIncidentsPublisher,
            MongoBaseUsersSessionsService mongoBaseUsersSessionsService,
            MongoUsersSessionsRepository mongoUsersSessionsRepository
    ) {
        return new MongoSessionRegistry(
                securityJwtEventsPublisher,
                securityJwtIncidentsPublisher,
                mongoBaseUsersSessionsService,
                mongoUsersSessionsRepository
        );
    }
}
