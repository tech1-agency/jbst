package jbst.iam.tests.converters.mongodb;

import jbst.iam.domain.mongodb.MongoDbUser;
import jbst.iam.domain.mongodb.MongoDbUserSession;
import lombok.experimental.UtilityClass;
import tech1.framework.foundation.domain.base.Username;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class MongoUserConverter {

    public static List<String> toUsernamesAsStrings1(List<MongoDbUser> users) {
        return Username.asStrings(users.stream().map(MongoDbUser::getUsername).collect(Collectors.toList()));
    }

    public static List<String> toUsernamesAsStrings2(List<MongoDbUserSession> sessions) {
        return Username.asStrings(sessions.stream().map(MongoDbUserSession::getUsername).collect(Collectors.toList()));
    }

    public static List<String> toAccessTokensAsStrings2(List<MongoDbUserSession> sessions) {
        return sessions.stream().map(session -> session.getAccessToken().value()).collect(Collectors.toList());
    }
}
