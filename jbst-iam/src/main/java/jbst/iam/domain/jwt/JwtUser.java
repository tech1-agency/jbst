package jbst.iam.domain.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.iam.domain.identifiers.UserId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZoneId;
import java.util.*;

import static jbst.foundation.domain.base.AbstractAuthority.*;
import static jbst.foundation.domain.constants.JbstConstants.ZoneIds.UKRAINE;
import static jbst.foundation.utilities.random.RandomUtility.*;
import static jbst.foundation.utilities.spring.SpringAuthoritiesUtility.getSimpleGrantedAuthorities;

public record JwtUser(
        UserId id,
        Username username,
        Password password,
        ZoneId zoneId,
        Set<SimpleGrantedAuthority> authorities,
        Email email,
        String name,
        boolean passwordChangeRequired,
        boolean emailConfirmationRequired,
        Map<String, Object> attributes
) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password.value();
    }

    @Override
    public String getUsername() {
        return this.username.value();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public static JwtUser hardcoded() {
        return hardcoded(
                getSimpleGrantedAuthorities("user")
        );
    }

    public static JwtUser hardcoded(Set<SimpleGrantedAuthority> authorities) {
        return new JwtUser(
                UserId.hardcoded(),
                Username.hardcoded(),
                Password.hardcoded(),
                UKRAINE,
                authorities,
                Email.hardcoded(),
                "",
                false,
                false,
                new HashMap<>()
        );
    }

    public static JwtUser hardcoded(Map<String, Object> attributes) {
        var user = JwtUser.hardcoded();
        user.attributes().putAll(attributes);
        return user;
    }

    public static JwtUser random() {
        return new JwtUser(
                UserId.random(),
                Username.random(),
                Password.random(),
                randomZoneId(),
                Set.of(
                        new SimpleGrantedAuthority(randomElement(List.of(SUPERADMIN, INVITATIONS_READ, INVITATIONS_WRITE)))
                ),
                Email.random(),
                randomString(),
                randomBoolean(),
                randomBoolean(),
                new HashMap<>(
                        Map.of(
                            randomString(), randomString(),
                            randomString(), randomInteger()
                        )
                )
        );
    }

    public static JwtUser randomSuperadmin() {
        return new JwtUser(
                UserId.random(),
                Username.random(),
                Password.random(),
                randomZoneId(),
                getSimpleGrantedAuthorities(SUPERADMIN),
                Email.random(),
                randomString(),
                false,
                false,
                new HashMap<>()
        );
    }

    @JsonIgnore
    public JwtTokenCreationParams getJwtTokenCreationParams() {
        return new JwtTokenCreationParams(
                this.username,
                this.authorities,
                this.zoneId
        );
    }

    @SuppressWarnings("unused")
    public boolean hasAllAuthorities(Set<SimpleGrantedAuthority> authorities) {
        return this.authorities.containsAll(authorities);
    }
}

