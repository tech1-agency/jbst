package io.tech1.framework.foundation.incidents.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.tech1.framework.foundation.domain.base.Password;
import io.tech1.framework.foundation.domain.base.Username;
import io.tech1.framework.foundation.domain.base.UsernamePasswordCredentials;
import io.tech1.framework.foundation.domain.http.requests.UserRequestMetadata;
import io.tech1.framework.foundation.domain.properties.base.SecurityJwtIncidentType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static io.tech1.framework.foundation.domain.constants.FrameworkLogsConstants.FRAMEWORK_INCIDENT_PREFIX;
import static io.tech1.framework.foundation.domain.constants.FrameworkLogsConstants.LINE_SEPARATOR_INTERPUNCT;
import static io.tech1.framework.foundation.domain.constants.StringConstants.COMMA_COLLECTORS;
import static io.tech1.framework.foundation.domain.constants.StringConstants.UNKNOWN;
import static io.tech1.framework.foundation.utilities.exceptions.TraceUtility.getTrace;
import static io.tech1.framework.foundation.utilities.random.RandomUtility.randomString;
import static io.tech1.framework.foundation.incidents.domain.IncidentAttributes.Keys.*;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
// Lombok
@Getter
@EqualsAndHashCode
@ToString
public class Incident {
    private final Map<String, Object> attributes;

    public Incident() {
        this.attributes = new TreeMap<>();
    }

    public Incident(Map<String, Object> attributes) {
        this.attributes = new TreeMap<>();
        this.addAll(attributes);
    }

    public Incident(String type) {
        this();
        this.setType(type);
    }

    public Incident(SecurityJwtIncidentType type) {
        this(type.getValue());
    }

    public Incident(SecurityJwtIncidentType type, Username username) {
        this(type);
        this.addUsername(username);
    }

    public Incident(SecurityJwtIncidentType type, UsernamePasswordCredentials credentials) {
        this(type);
        this.addUsername(credentials.username());
        this.addPassword(credentials.password());
    }

    public Incident(SecurityJwtIncidentType type, Username username, UserRequestMetadata userRequestMetadata) {
        this(type);
        this.addUsername(username);
        this.addUserRequestMetadata(userRequestMetadata);
    }

    public Incident(SecurityJwtIncidentType type, UsernamePasswordCredentials credentials, UserRequestMetadata userRequestMetadata) {
        this(type, credentials);
        this.addUserRequestMetadata(userRequestMetadata);
    }

    public Incident(Throwable throwable) {
        this(IncidentAttributes.IncidentsTypes.THROWABLE);

        this.add(IncidentAttributes.Keys.EXCEPTION, throwable.getClass());
        this.add(IncidentAttributes.Keys.TRACE, getTrace(throwable));
        this.add(IncidentAttributes.Keys.MESSAGE, throwable.getMessage());
    }

    public Incident(Throwable throwable, Method method, List<Object> params) {
        this(throwable);

        if (nonNull(method)) {
            this.add(IncidentAttributes.Keys.METHOD, method.toString());
        }

        if (!isEmpty(params)) {
            this.add(IncidentAttributes.Keys.PARAMS, params.stream().map(Object::toString).collect(Collectors.joining(COMMA_COLLECTORS)));
        }
    }

    public Incident(Throwable throwable, Map<String, Object> attributes) {
        this(throwable);

        if (!isEmpty(attributes)) {
            attributes.forEach(this::add);
        }
    }

    public static Incident random() {
        var incident = new Incident(randomString());
        incident.add(randomString(), randomString());
        incident.add(randomString(), randomString());
        incident.add(randomString(), randomString());
        return incident;
    }

    public static Incident copyOf(@NotNull Incident incident) {
        var instance = new Incident();
        instance.addAll(incident.getAttributes());
        return instance;
    }

    public void add(String key, Object value) {
        this.attributes.put(key, value);
    }

    public void addAll(Map<String, Object> attributes) {
        this.attributes.putAll(attributes);
    }

    public void print() {
        LOGGER.info(LINE_SEPARATOR_INTERPUNCT);
        LOGGER.info(FRAMEWORK_INCIDENT_PREFIX + " IncidentType: `{}`", this.getType());
        this.attributes.entrySet().stream()
                .filter(entry -> !IncidentAttributes.Keys.TYPE.equals(entry.getKey()))
                .forEach(entry -> LOGGER.info(entry.getKey() + " — " + entry.getValue()));
        LOGGER.info(LINE_SEPARATOR_INTERPUNCT);
    }

    public void setType(String type) {
        this.add(IncidentAttributes.Keys.TYPE, type);
    }

    public void addUsername(Username username) {
        this.add(IncidentAttributes.Keys.USERNAME, username);
    }

    public void addPassword(Password password) {
        this.add(IncidentAttributes.Keys.PASSWORD, password);
    }

    public void addUserRequestMetadata(UserRequestMetadata userRequestMetadata) {
        var tupleResponseException = userRequestMetadata.getException();
        if (!tupleResponseException.isOk()) {
            this.add(EXCEPTION, tupleResponseException.getMessage());
        }

        var whereTuple3 = userRequestMetadata.getWhereTuple3();
        this.add(IP_ADDRESS, whereTuple3.a());
        this.add(COUNTRY_FLAG, whereTuple3.b());
        this.add(WHERE, whereTuple3.c());

        var whatTuple2 = userRequestMetadata.getWhatTuple2();
        this.add(BROWSER, whatTuple2.a());
        this.add(WHAT, whatTuple2.b());
    }

    @JsonIgnore
    public String getType() {
        var attribute = this.attributes.get(IncidentAttributes.Keys.TYPE);
        if (nonNull(attribute)) {
            return attribute.toString();
        } else {
            return UNKNOWN;
        }
    }

    @JsonIgnore
    public Username getUsername() {
        var attribute = this.attributes.get(IncidentAttributes.Keys.USERNAME);
        if (nonNull(attribute)) {
            return Username.of(attribute.toString());
        } else {
            return Username.of(UNKNOWN);
        }
    }
}
