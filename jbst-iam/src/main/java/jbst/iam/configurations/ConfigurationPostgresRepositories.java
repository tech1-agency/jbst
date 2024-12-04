package jbst.iam.configurations;

import jbst.iam.repositories.postgres.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EntityScan({
        "jbst.iam.domain.postgres"
})
@EnableJpaRepositories({
        "jbst.iam.repositories.postgres"
})
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationPostgresRepositories {

    // Repositories
    private final PostgresInvitationsRepository invitationsRepository;
    private final PostgresUsersTokensRepository usersTokensRepository;
    private final PostgresUsersRepository userRepository;
    private final PostgresUsersSessionsRepository userSessionRepository;

    @Bean
    public JbstPostgresRepositories jbstPostgresRepositories() {
        return new JbstPostgresRepositories(
                this.invitationsRepository,
                this.usersTokensRepository,
                this.userRepository,
                this.userSessionRepository
        );
    }
}
