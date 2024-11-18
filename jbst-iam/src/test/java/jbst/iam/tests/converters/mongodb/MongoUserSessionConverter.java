package jbst.iam.tests.converters.mongodb;

import jbst.iam.domain.mongodb.MongoDbUserSession;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class MongoUserSessionConverter {

    public static Set<Boolean> toMetadataRenewCron(List<MongoDbUserSession> sessions) {
        return sessions.stream().map(MongoDbUserSession::isMetadataRenewCron).collect(Collectors.toSet());
    }
}
