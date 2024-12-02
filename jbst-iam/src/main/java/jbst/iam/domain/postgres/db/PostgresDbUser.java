package jbst.iam.domain.postgres.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.constants.JbstConstants;
import jbst.foundation.domain.converters.columns.*;
import jbst.iam.converters.columns.PostgresSetOfSimpleGrantedAuthoritiesConverter;
import jbst.iam.domain.identifiers.UserId;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.postgres.superclasses.PostgresDbAbstractPersistable0;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.nonNull;
import static jbst.foundation.domain.base.AbstractAuthority.*;
import static jbst.foundation.utilities.random.RandomUtility.*;
import static jbst.foundation.utilities.spring.SpringAuthoritiesUtility.getSimpleGrantedAuthorities;
import static org.springframework.util.StringUtils.capitalize;

@SuppressWarnings("JpaDataSourceORMInspection")
// Lombok
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
// JPA
@Entity
@Table(name = PostgresDbUser.PG_TABLE_NAME)
public class PostgresDbUser extends PostgresDbAbstractPersistable0 {
    public static final String PG_TABLE_NAME = "jbst_users";

    @Convert(converter = PostgresUsernameConverter.class)
    @Column(nullable = false, updatable = false)
    private Username username;

    @Convert(converter = PostgresPasswordConverter.class)
    @Column(nullable = false)
    private Password password;

    @Schema(type = "string")
    @Convert(converter = PostgresZoneIdConverter.class)
    @Column(name = "zone_id", nullable = false)
    private ZoneId zoneId;

    @Convert(converter = PostgresSetOfSimpleGrantedAuthoritiesConverter.class)
    @Column(length = 1024, nullable = false)
    private Set<SimpleGrantedAuthority> authorities;

    @Convert(converter = PostgresEmailConverter.class)
    @Column
    private Email email;

    @Column
    private String name;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired;

    @Column(name = "email_confirmation_required", nullable = false)
    private boolean emailConfirmationRequired;

    @Convert(converter = PostgresMapStringsObjectsConverter.class)
    @Column(length = 65535)
    private Map<String, Object> attributes;

    public PostgresDbUser(
            Username username,
            Password password,
            ZoneId zoneId,
            Set<SimpleGrantedAuthority> authorities,
            boolean passwordChangeRequired
    ) {
        this.username = username;
        this.password = password;
        this.zoneId = zoneId;
        this.authorities = authorities;
        this.passwordChangeRequired = passwordChangeRequired;
        this.emailConfirmationRequired = false;
        this.attributes = new HashMap<>();
    }

    public PostgresDbUser(
            Username username,
            Password password,
            ZoneId zoneId,
            Email email,
            Set<SimpleGrantedAuthority> authorities,
            boolean passwordChangeRequired
    ) {
        this.username = username;
        this.password = password;
        this.zoneId = zoneId;
        this.email = email;
        this.authorities = authorities;
        this.passwordChangeRequired = passwordChangeRequired;
        this.emailConfirmationRequired = true;
        this.attributes = new HashMap<>();
    }

    public PostgresDbUser(JwtUser user) {
        this.id = user.id().value();
        this.username = user.username();
        this.password = user.password();
        this.zoneId = user.zoneId();
        this.authorities = user.authorities();
        this.email = user.email();
        this.name = user.name();
        this.passwordChangeRequired = user.passwordChangeRequired();
        this.attributes = user.attributes();
    }

    public static PostgresDbUser random(String username, Set<String> authorities) {
        var user = new PostgresDbUser(
                Username.of(username),
                Password.random(),
                randomZoneId(),
                getSimpleGrantedAuthorities(authorities),
                randomBoolean()
        );
        user.setEmail(Email.of(username + "@" + JbstConstants.Domains.HARDCODED));
        user.setName(capitalize(randomString()) + " " + capitalize(randomString()));
        user.setAttributes(
                Map.of(
                        randomString(), randomString(),
                        randomString(), randomLong()
                )
        );
        return user;
    }

    public static PostgresDbUser random(String username, String authority) {
        return random(username, Set.of(authority));
    }

    public static PostgresDbUser randomSuperadmin(String username) {
        return random(username, SUPERADMIN);
    }

    public static PostgresDbUser randomAdmin(String username) {
        return random(username, "admin");
    }

    public static List<PostgresDbUser> dummies1() {
        return List.of(
                PostgresDbUser.randomSuperadmin("sa1"),
                PostgresDbUser.randomSuperadmin("sa2"),
                PostgresDbUser.randomAdmin("admin1"),
                PostgresDbUser.random("user1", Set.of("user", INVITATIONS_WRITE)),
                PostgresDbUser.random("user2", Set.of("user", INVITATIONS_READ)),
                PostgresDbUser.random("sa3", Set.of(INVITATIONS_READ, SUPERADMIN, INVITATIONS_WRITE))
        );
    }

    @SuppressWarnings("unused")
    @JsonIgnore
    public Map<String, Object> getNotNullAttributes() {
        return nonNull(this.attributes) ? this.attributes : new HashMap<>();
    }

    @JsonIgnore
    @Transient
    public UserId userId() {
        return new UserId(this.id);
    }

    @JsonIgnore
    @Transient
    public JwtUser asJwtUser() {
        return new JwtUser(
                this.userId(),
                this.username,
                this.password,
                this.zoneId,
                this.authorities,
                this.email,
                this.name,
                this.passwordChangeRequired,
                this.emailConfirmationRequired,
                this.attributes
        );
    }
}
