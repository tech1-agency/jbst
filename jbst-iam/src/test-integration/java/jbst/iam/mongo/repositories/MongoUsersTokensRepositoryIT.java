package jbst.iam.mongo.repositories;

import jbst.foundation.utilities.random.RandomUtility;
import jbst.iam.configurations.ConfigurationMongoRepositories;
import jbst.iam.domain.db.UserToken;
import jbst.iam.domain.dto.requests.RequestUserToken;
import jbst.iam.domain.identifiers.TokenId;
import jbst.iam.domain.mongodb.MongoDbUserToken;
import jbst.iam.mongo.configs.MongoBeforeAllCallback;
import jbst.iam.mongo.configs.TestsConfigurationMongoRepositoriesRunner;
import jbst.iam.repositories.mongodb.MongoUsersTokensRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.repository.MongoRepository;

import static jbst.foundation.utilities.random.EntityUtility.entity;
import static jbst.foundation.utilities.random.RandomUtility.randomElement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ExtendWith({
        MongoBeforeAllCallback.class
})
@SpringBootTest(
        webEnvironment = NONE,
        classes = {
                ConfigurationMongoRepositories.class
        }
)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class MongoUsersTokensRepositoryIT extends TestsConfigurationMongoRepositoriesRunner {

    private final MongoUsersTokensRepository usersTokensRepository;

    @Override
    public MongoRepository<?, String> getMongoRepository() {
        return this.usersTokensRepository;
    }

    @Test
    void readIntegrationTests() {
        // Arrange
        var saved = this.usersTokensRepository.saveAll(MongoDbUserToken.dummies1());

        var notExistentTokenId = entity(TokenId.class);
        var notExistentToken = RandomUtility.randomString();

        var savedToken = saved.get(0);
        var existentTokenId = savedToken.tokenId();
        var existentToken = savedToken.getValue();
        var savedExpiredToken = saved.get(3);
        var expiredTokenId = savedExpiredToken.tokenId();
        var expiredToken = savedExpiredToken.getValue();
        var savedUsedToken = saved.get(5);
        var usedTokenId = savedUsedToken.tokenId();
        var usedToken = savedUsedToken.getValue();

        // Act
        var count = this.usersTokensRepository.count();

        // Assert
        assertThat(count).isEqualTo(6);
        assertThat(this.usersTokensRepository.findById(existentTokenId.value())).isNotEmpty();
        assertThat(this.usersTokensRepository.findById(notExistentTokenId.value())).isEmpty();
        assertThat(this.usersTokensRepository.findByValueAsAny(existentToken)).isNotNull();
        assertThat(this.usersTokensRepository.findByValueAsAny(notExistentToken)).isNull();
        assertThat(this.usersTokensRepository.findById(expiredTokenId.value())).isNotEmpty();
        assertThat(this.usersTokensRepository.findByValueAsAny(expiredToken)).isNotNull();
        assertThat(this.usersTokensRepository.findById(usedTokenId.value())).isNotEmpty();
        assertThat(this.usersTokensRepository.findByValueAsAny(usedToken)).isNotNull();
    }

    @Test
    void deletionIntegrationTests() {
        // Arrange
        var saved = this.usersTokensRepository.saveAll(MongoDbUserToken.dummies1());
        var savedExpiredToken = saved.get(3);
        var expiredTokenId = savedExpiredToken.tokenId();
        var expiredToken = savedExpiredToken.getValue();
        var savedUsedToken = saved.get(5);
        var usedTokenId = savedUsedToken.tokenId();
        var usedToken = savedUsedToken.getValue();

        // Act-Assert-0
        assertThat(this.usersTokensRepository.count()).isEqualTo(6);

        // Act-Assert-1
        this.usersTokensRepository.cleanupExpired();
        assertThat(this.usersTokensRepository.count()).isEqualTo(4);
        assertThat(this.usersTokensRepository.findById(expiredTokenId.value())).isEmpty();
        assertThat(this.usersTokensRepository.findByValueAsAny(expiredToken)).isNull();

        // Act-Assert-2
        this.usersTokensRepository.cleanupUsed();
        assertThat(this.usersTokensRepository.count()).isEqualTo(2);
        assertThat(this.usersTokensRepository.findById(usedTokenId.value())).isEmpty();
        assertThat(this.usersTokensRepository.findByValueAsAny(usedToken)).isNull();
    }

    @Test
    void saveIntegrationTests() {
        // Arrange
        var saved = this.usersTokensRepository.saveAll(MongoDbUserToken.dummies1());

        // Act-Assert-0
        assertThat(this.usersTokensRepository.count()).isEqualTo(6);

        // Act-Assert-1
        this.usersTokensRepository.saveAs(randomElement(saved).asUserToken());
        assertThat(this.usersTokensRepository.count()).isEqualTo(6);

        // Act-Assert-2
        var existentTokenId = this.usersTokensRepository.saveAs(UserToken.random());
        assertThat(this.usersTokensRepository.count()).isEqualTo(7);
        var notExistentTokenId = entity(TokenId.class);
        assertThat(this.usersTokensRepository.findById(existentTokenId.value())).isNotEmpty();
        assertThat(this.usersTokensRepository.findById(notExistentTokenId.value())).isEmpty();

        // Act-Assert-3
        var requestUserEmailToken = RequestUserToken.hardcoded();
        var userEmailToken = this.usersTokensRepository.saveAs(requestUserEmailToken);
        assertThat(this.usersTokensRepository.count()).isEqualTo(8);
        assertThat(this.usersTokensRepository.findById(userEmailToken.id().value())).isNotEmpty();
        assertThat(this.usersTokensRepository.findByValueAsAny(userEmailToken.value())).isNotNull();

        // Act-Assert-4
        var savedToken = saved.get(0);
        assertThat(savedToken.isUsed()).isFalse();
        savedToken.setUsed(true);
        var tokenId = this.usersTokensRepository.saveAs(savedToken.asUserToken());
        var updatedToken = this.usersTokensRepository.findById(tokenId.value());
        assertThat(updatedToken).isNotEmpty();
        assertThat(updatedToken.get().isUsed()).isTrue();
        var updatedAnyToken = this.usersTokensRepository.findByValueAsAny(savedToken.getValue());
        assertThat(updatedAnyToken).isNotNull();
        assertThat(updatedAnyToken.used()).isTrue();
    }
}
