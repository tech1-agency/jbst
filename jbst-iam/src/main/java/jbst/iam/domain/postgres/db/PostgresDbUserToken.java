package jbst.iam.domain.postgres.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.converters.columns.PostgresUsernameConverter;
import jbst.foundation.domain.time.TimeAmount;
import jbst.iam.domain.db.UserToken;
import jbst.iam.domain.dto.requests.RequestUserToken;
import jbst.iam.domain.enums.UserTokenType;
import jbst.iam.domain.identifiers.TokenId;
import jbst.iam.domain.postgres.superclasses.PostgresDbAbstractPersistable0;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Transient;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static jbst.foundation.utilities.random.RandomUtility.randomEnum;
import static jbst.foundation.utilities.random.RandomUtility.randomStringLetterOrNumbersOnly;
import static jbst.foundation.utilities.time.TimestampUtility.getFutureRange;
import static jbst.foundation.utilities.time.TimestampUtility.getPastRange;

@SuppressWarnings("JpaDataSourceORMInspection")
// Lombok
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
// JPA
@Entity
@Table(name = PostgresDbUserToken.PG_TABLE_NAME)
public class PostgresDbUserToken extends PostgresDbAbstractPersistable0 {
    public static final String PG_TABLE_NAME = "jbst_users_tokens";

    @Convert(converter = PostgresUsernameConverter.class)
    @Column(nullable = false, updatable = false)
    private Username username;

    @Column(length = 36, nullable = false, updatable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private UserTokenType type;

    @Column(name = "expiry_timestamp", nullable = false, updatable = false)
    private long expiryTimestamp;

    @Column(nullable = false)
    private boolean used;

    public PostgresDbUserToken(
            @NotNull Username username,
            @NotNull String value,
            @NotNull UserTokenType type,
            long expiryTimestamp,
            boolean used
    ) {
        this.username = username;
        this.value = value;
        this.type = type;
        this.expiryTimestamp = expiryTimestamp;
        this.used = used;
    }

    public PostgresDbUserToken(RequestUserToken request) {
        this(
                request.username(),
                randomStringLetterOrNumbersOnly(36),
                request.type(),
                getFutureRange(new TimeAmount(24, ChronoUnit.HOURS)).to(),
                false
        );
    }

    public PostgresDbUserToken(UserToken token) {
        this.id = token.id().value();
        this.username = token.username();
        this.value = token.value();
        this.type = token.type();
        this.expiryTimestamp = token.expiryTimestamp();
        this.used = token.used();
    }

    public static PostgresDbUserToken random(
            Username username,
            long expiryTimestamp,
            boolean used
    ) {
        return new PostgresDbUserToken(
                username,
                randomStringLetterOrNumbersOnly(36),
                randomEnum(UserTokenType.class),
                expiryTimestamp,
                used
        );
    }

    public static List<PostgresDbUserToken> dummies1() {
        var token1 = PostgresDbUserToken.random(
                Username.of("username1"),
                getFutureRange(new TimeAmount(1, ChronoUnit.DAYS)).to(),
                false
        );
        var token2 = PostgresDbUserToken.random(
                Username.of("username2"),
                getFutureRange(new TimeAmount(1, ChronoUnit.DAYS)).to(),
                false
        );
        var token3 = PostgresDbUserToken.random(
                Username.of("username3"),
                getPastRange(new TimeAmount(1, ChronoUnit.DAYS)).from(),
                false
        );
        var token4 = PostgresDbUserToken.random(
                Username.of("username4"),
                getPastRange(new TimeAmount(1, ChronoUnit.DAYS)).from(),
                false
        );
        var token5 = PostgresDbUserToken.random(
                Username.of("username1"),
                getFutureRange(new TimeAmount(1, ChronoUnit.DAYS)).to(),
                true
        );
        var token6 = PostgresDbUserToken.random(
                Username.of("username2"),
                getFutureRange(new TimeAmount(1, ChronoUnit.DAYS)).to(),
                true
        );
        return List.of(
                token1,
                token2,
                token3,
                token4,
                token5,
                token6
        );
    }

    @JsonIgnore
    @Transient
    public TokenId tokenId() {
        return new TokenId(this.id);
    }

    @JsonIgnore
    @Transient
    public UserToken asUserToken() {
        return new UserToken(
                new TokenId(this.id),
                this.username,
                this.value,
                this.type,
                this.expiryTimestamp,
                this.used
        );
    }
}
