package jbst.iam.repositories.postgres;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.tuples.TuplePresence;
import jbst.iam.domain.db.Invitation;
import jbst.iam.domain.dto.requests.RequestUserRegistration0;
import jbst.iam.domain.dto.requests.RequestUserRegistration1;
import jbst.iam.domain.identifiers.UserId;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.postgres.db.PostgresDbUser;
import jbst.iam.domain.postgres.projections.PostgresDbUserProjection1;
import jbst.iam.repositories.UsersRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static jbst.foundation.domain.constants.JbstConstants.SpringAuthorities.SUPERADMIN;
import static jbst.foundation.domain.tuples.TuplePresence.present;
import static jbst.foundation.utilities.exceptions.ExceptionsMessagesUtility.entityNotFound;

@SuppressWarnings({"JpaQlInspection", "SqlNoDataSourceInspection"})
public interface PostgresUsersRepository extends JpaRepository<PostgresDbUser, String>, UsersRepository {
    // ================================================================================================================
    // Any
    // ================================================================================================================
    default TuplePresence<JwtUser> isPresent(UserId userId) {
        return this.findById(userId.value())
                .map(entity -> present(entity.asJwtUser()))
                .orElseGet(TuplePresence::absent);
    }

    default JwtUser loadUserByUsername(Username username) throws UsernameNotFoundException {
        var user = this.findByUsername(username);
        if (nonNull(user)) {
            return user.asJwtUser();
        } else {
            throw new UsernameNotFoundException(entityNotFound("Username", username.value()));
        }
    }

    default JwtUser findByUsernameAsJwtUserOrNull(Username username) {
        var user = this.findByUsername(username);
        return nonNull(user) ? user.asJwtUser() : null;
    }

    default JwtUser findByEmailAsJwtUserOrNull(Email email) {
        var user = this.findByEmail(email);
        return nonNull(user) ? user.asJwtUser() : null;
    }

    default UserId saveAs(JwtUser user) {
        var entity = this.save(new PostgresDbUser(user));
        return entity.userId();
    }

    default UserId saveAs(RequestUserRegistration0 requestUserRegistration0, Password password) {
        var user = new PostgresDbUser(
                requestUserRegistration0.username(),
                password,
                requestUserRegistration0.zoneId(),
                requestUserRegistration0.email(),
                new HashSet<>(),
                false
        );
        var entity = this.save(user);
        return entity.userId();
    }

    default UserId saveAs(RequestUserRegistration1 requestUserRegistration1, Password password, Invitation invitation) {
        var user = new PostgresDbUser(
                requestUserRegistration1.username(),
                password,
                requestUserRegistration1.zoneId(),
                invitation.authorities(),
                false
        );
        var entity = this.save(user);
        return entity.userId();
    }

    // ================================================================================================================
    // Spring Data
    // ================================================================================================================
    PostgresDbUser findByEmail(Email email);
    PostgresDbUser findByUsername(Username username);
    List<PostgresDbUser> findByUsernameIn(Set<Username> usernames);
    List<PostgresDbUser> findByUsernameIn(List<Username> usernames);

    // ================================================================================================================
    // Queries
    // ================================================================================================================
    @Query(value = "SELECT * FROM " + PostgresDbUser.PG_TABLE_NAME + " u WHERE u.authorities LIKE %:authority%", nativeQuery = true)
    List<PostgresDbUser> findByAuthority(@Param("authority") String authority);

    @Query(value = "SELECT u.username FROM " + PostgresDbUser.PG_TABLE_NAME + " u WHERE u.authorities LIKE %:authority%", nativeQuery = true)
    List<PostgresDbUserProjection1> findByAuthorityProjectionUsernames(@Param("authority") String authority);

    @Query(value = "SELECT * FROM " + PostgresDbUser.PG_TABLE_NAME + " u WHERE u.authorities NOT LIKE %:authority%", nativeQuery = true)
    List<PostgresDbUser> findByAuthorityNotEqual(@Param("authority") String authority);

    @Query(value = "SELECT * FROM " + PostgresDbUser.PG_TABLE_NAME + " u WHERE u.authorities NOT LIKE %:authority%", nativeQuery = true)
    List<PostgresDbUserProjection1> findByAuthorityNotEqualProjectionUsernames(@Param("authority") String authority);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM " + PostgresDbUser.PG_TABLE_NAME + " u WHERE u.authorities LIKE %:authority%", nativeQuery = true)
    void deleteByAuthority(@Param("authority") String authority);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM " + PostgresDbUser.PG_TABLE_NAME + " u WHERE u.authorities NOT LIKE %:authority%", nativeQuery = true)
    void deleteByAuthorityNotEqual(@Param("authority") String authority);

    default List<PostgresDbUser> findByAuthoritySuperadmin() {
        return this.findByAuthority(SUPERADMIN.getAuthority());
    }

    default Set<Username> findSuperadminsUsernames() {
        return this.findByAuthorityProjectionUsernames(SUPERADMIN.getAuthority()).stream().map(PostgresDbUserProjection1::getAsUsername).collect(Collectors.toSet());
    }

    default List<PostgresDbUser> findByAuthorityNotSuperadmin() {
        return this.findByAuthorityNotEqual(SUPERADMIN.getAuthority());
    }

    default Set<Username> findNotSuperadminsUsernames() {
        return this.findByAuthorityNotEqualProjectionUsernames(SUPERADMIN.getAuthority()).stream().map(PostgresDbUserProjection1::getAsUsername).collect(Collectors.toSet());
    }

    @Transactional
    default void deleteByAuthoritySuperadmin() {
        this.deleteByAuthority(SUPERADMIN.getAuthority());
    }

    @Transactional
    default void deleteByAuthorityNotSuperadmin() {
        this.deleteByAuthorityNotEqual(SUPERADMIN.getAuthority());
    }
}
