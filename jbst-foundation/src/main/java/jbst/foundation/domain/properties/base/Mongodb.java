package jbst.foundation.domain.properties.base;

import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.properties.annotations.MandatoryProperty;
import jbst.foundation.domain.properties.annotations.NonMandatoryProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import static java.util.Objects.nonNull;
import static jbst.foundation.utilities.random.RandomUtility.*;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class Mongodb extends AbstractPropertyConfigs {
    @MandatoryProperty
    private final String host;
    @MandatoryProperty
    private final Integer port;
    @MandatoryProperty
    private final String database;
    @NonMandatoryProperty
    private Username username;
    @NonMandatoryProperty
    private Password password;

    public static Mongodb hardcoded() {
        return Mongodb.noSecurity("127.0.0.1", 27017, "jbst");
    }

    public static Mongodb random() {
        return Mongodb.noSecurity(randomIPv4(), randomIntegerGreaterThanZeroByBounds(26000, 30000), randomString());
    }

    public static Mongodb noSecurity(String host, int port, String database) {
        return new Mongodb(host, port, database, null, null);
    }

    public final String connectionString() {
        if (isAuthenticationRequired()) {
            return "mongodb://" + this.username.value() + ":" + this.password.value() + "@" + this.host + ":" + this.port + "/" + this.database;
        } else {
            return "mongodb://" + this.host + ":" + this.port + "/" + this.database;
        }
    }

    // ================================================================================================================
    // PRIVATE METHODS
    // ================================================================================================================
    private boolean isAuthenticationRequired() {
        return nonNull(this.username) && nonNull(this.password);
    }
}
