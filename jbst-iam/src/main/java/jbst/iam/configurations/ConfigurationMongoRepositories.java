package jbst.iam.configurations;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jbst.iam.repositories.mongodb.MongoInvitationsRepository;
import jbst.iam.repositories.mongodb.MongoUsersRepository;
import jbst.iam.repositories.mongodb.MongoUsersSessionsRepository;
import jbst.iam.repositories.mongodb.JbstMongoRepositories;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import jbst.foundation.domain.properties.JbstProperties;

@Configuration
@EnableConfigurationProperties({
        JbstProperties.class
})
@EntityScan({
        "jbst.iam.domain.mongo"
})
@EnableMongoRepositories(
        basePackages = "jbst.iam.repositories",
        mongoTemplateRef = "jbstMongoTemplate"
)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationMongoRepositories {

    // Properties
    private final JbstProperties jbstProperties;

    @Bean
    public JbstMongoRepositories jbstMongoRepositories(
            MongoInvitationsRepository mongoInvitationsRepository,
            MongoUsersRepository mongoUsersRepository,
            MongoUsersSessionsRepository userSessionRepository
    ) {
        return new JbstMongoRepositories(
                mongoInvitationsRepository,
                mongoUsersRepository,
                userSessionRepository
        );
    }

    @Bean
    public MongoClient jbstMongoClient() {
        var mongodb = this.jbstProperties.getMongodbSecurityJwtConfigs().getMongodb();
        var connectionString = new ConnectionString(mongodb.connectionString());
        var mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoDatabaseFactory jbstMongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(
                this.jbstMongoClient(),
                this.jbstProperties.getMongodbSecurityJwtConfigs().getMongodb().getDatabase()
        );
    }

    @Bean
    public MongoTemplate jbstMongoTemplate() {
        var dbRefResolver = new DefaultDbRefResolver(this.jbstMongoDatabaseFactory());
        var mongoConverter = new MappingMongoConverter(dbRefResolver, new MongoMappingContext());
        return new MongoTemplate(
                this.jbstMongoDatabaseFactory(),
                mongoConverter
        );
    }
}
