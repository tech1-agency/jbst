package jbst.iam.postgres.repositories;

import jbst.iam.configurations.ConfigurationPostgresRepositories;
import jbst.iam.domain.db.InvitationCode;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.domain.identifiers.UserId;
import jbst.iam.domain.postgres.db.PostgresDbUser;
import jbst.iam.postgres.configs.PostgresBeforeAllCallback;
import jbst.iam.postgres.configs.TestsConfigurationPostgresRepositoriesRunner;
import jbst.iam.repositories.postgres.PostgresUsersRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.constants.JbstConstants;
import jbst.foundation.domain.tuples.TuplePresence;

import java.util.List;
import java.util.Set;

import static jbst.iam.domain.jwt.JwtUser.randomSuperadmin;
import static jbst.iam.tests.converters.postgres.PostgresUserConverter.toUsernamesAsStrings1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.entityNotFound;
import static jbst.foundation.utilities.random.EntityUtility.entity;
import static jbst.foundation.utilities.random.RandomUtility.randomElement;

@ExtendWith({
        PostgresBeforeAllCallback.class
})
@SpringBootTest(
        webEnvironment = NONE,
        classes = {
                ConfigurationPostgresRepositories.class
        }
)
@AutoConfigureDataJpa
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class PostgresUsersRepositoryIT extends TestsConfigurationPostgresRepositoriesRunner {

    private final PostgresUsersRepository usersRepository;

    @Override
    public JpaRepository<PostgresDbUser, String> getJpaRepository() {
        return this.usersRepository;
    }

    @Test
    void readIntegrationTests() {
        // Arrange
        var saved = this.usersRepository.saveAll(PostgresDbUser.dummies1());

        var notExistentUserId = entity(UserId.class);

        var savedUser = saved.get(0);
        var existentUserId = savedUser.userId();

        // Act
        var count = this.usersRepository.count();

        // Assert
        assertThat(count).isEqualTo(6);
        assertThat(this.usersRepository.isPresent(existentUserId)).isEqualTo(TuplePresence.present(savedUser.asJwtUser()));
        assertThat(this.usersRepository.isPresent(notExistentUserId)).isEqualTo(TuplePresence.absent());
        assertThat(this.usersRepository.loadUserByUsername(Username.of("sa1"))).isNotNull();
        assertThat(catchThrowable(() -> this.usersRepository.loadUserByUsername(Username.of("sa777"))))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageStartingWith(entityNotFound("Username", "sa777"));
        assertThat(this.usersRepository.findByUsernameAsJwtUserOrNull(Username.of("sa2"))).isNotNull();
        assertThat(this.usersRepository.findByUsernameAsJwtUserOrNull(Username.of("sa888"))).isNull();
        assertThat(this.usersRepository.findByEmailAsJwtUserOrNull(Email.of("sa3@" + JbstConstants.Domains.HARDCODED))).isNotNull();
        assertThat(this.usersRepository.findByEmailAsJwtUserOrNull(Email.of("sa999@" + JbstConstants.Domains.HARDCODED))).isNull();
        assertThat(this.usersRepository.findByEmail(Email.of("sa1@" + JbstConstants.Domains.HARDCODED))).isNotNull();
        assertThat(this.usersRepository.findByEmail(Email.of("sa2@" + JbstConstants.Domains.HARDCODED))).isNotNull();
        assertThat(this.usersRepository.findByEmail(Email.of("sa4@" + JbstConstants.Domains.HARDCODED))).isNull();
        assertThat(this.usersRepository.findByUsername(Username.of("sa1"))).isNotNull();
        assertThat(this.usersRepository.findByUsername(Username.of("sa2"))).isNotNull();
        assertThat(this.usersRepository.findByUsername(Username.of("sa4"))).isNull();
        assertThat(this.usersRepository.findByUsernameIn(
                Set.of(
                        Username.of("sa1"),
                        Username.of("admin1"),
                        Username.of("not_real1")
                )
        )).hasSize(2);
        assertThat(this.usersRepository.findByUsernameIn(
                List.of(
                        Username.of("sa3"),
                        Username.of("user1"),
                        Username.of("not_real2")
                )
        )).hasSize(2);

        assertThat(toUsernamesAsStrings1(this.usersRepository.findByAuthoritySuperadmin()))
                .hasSize(3)
                .containsExactlyInAnyOrder("sa1", "sa2", "sa3");

        assertThat(toUsernamesAsStrings1(this.usersRepository.findByAuthorityNotSuperadmin()))
                .hasSize(3)
                .containsExactlyInAnyOrder("admin1", "user1", "user2");

        assertThat(Username.asStrings(this.usersRepository.findSuperadminsUsernames()))
                .hasSize(3)
                .containsExactlyInAnyOrder("sa1", "sa2", "sa3");

        assertThat(Username.asStrings(this.usersRepository.findNotSuperadminsUsernames()))
                .hasSize(3)
                .containsExactlyInAnyOrder("admin1", "user1", "user2");

        var jwtUser = this.usersRepository.loadUserByUsername(Username.of("sa1"));
        assertThat(jwtUser).isNotNull();
        assertThat(jwtUser.username()).isEqualTo(Username.of("sa1"));
        assertThat(jwtUser.password()).isNotNull();
        assertThat(jwtUser.authorities()).isNotNull();
        assertThat(jwtUser.isAccountNonExpired()).isTrue();
        assertThat(jwtUser.isAccountNonLocked()).isTrue();
        assertThat(jwtUser.isCredentialsNonExpired()).isTrue();
        assertThat(jwtUser.isEnabled()).isTrue();

        var username = Username.random();
        var throwable = catchThrowable(() -> this.usersRepository.loadUserByUsername(username));
        assertThat(throwable)
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageStartingWith(entityNotFound("Username", username.value()));
    }

    @Test
    void deletionIntegrationTests() {
        // Arrange
        this.usersRepository.saveAll(PostgresDbUser.dummies1());

        // Act-Assert-0
        assertThat(this.usersRepository.count()).isEqualTo(6);

        // Act-Assert-1
        this.usersRepository.deleteByAuthorityNotSuperadmin();
        assertThat(toUsernamesAsStrings1(this.usersRepository.findAll()))
                .hasSize(3)
                .containsExactlyInAnyOrder("sa1", "sa2", "sa3");

        // Act-Assert-2
        this.usersRepository.deleteByAuthoritySuperadmin();
        assertThat(this.usersRepository.count()).isZero();
    }

    @Test
    void saveIntegrationTests() {
        // Arrange
        var saved = this.usersRepository.saveAll(PostgresDbUser.dummies1());

        // Act-Assert-0
        assertThat(this.usersRepository.count()).isEqualTo(6);

        // Act-Assert-1
        this.usersRepository.saveAs(randomElement(saved).asJwtUser());
        assertThat(this.usersRepository.count()).isEqualTo(6);

        // Act-Assert-2
        var userId1 = this.usersRepository.saveAs(randomSuperadmin());
        assertThat(this.usersRepository.count()).isEqualTo(7);
        assertThat(userId1).isNotNull();
        assertThat(this.usersRepository.isPresent(userId1).present()).isTrue();
        assertThat(this.usersRepository.isPresent(entity(UserId.class)).present()).isFalse();

        // Act-Assert-1
        var userId2 = this.usersRepository.saveAs(RequestUserRegistration1.testsHardcoded(), Password.random(), InvitationCode.random());
        assertThat(this.usersRepository.count()).isEqualTo(8);
        assertThat(this.usersRepository.findByUsernameAsJwtUserOrNull(Username.of("registration11")).id()).isEqualTo(userId2);
    }
}
