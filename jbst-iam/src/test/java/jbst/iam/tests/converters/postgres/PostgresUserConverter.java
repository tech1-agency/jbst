package jbst.iam.tests.converters.postgres;

import jbst.iam.domain.postgres.db.PostgresDbUser;
import jbst.iam.domain.postgres.db.PostgresDbUserSession;
import lombok.experimental.UtilityClass;
import tech1.framework.foundation.domain.base.Username;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class PostgresUserConverter {

    public static List<String> toUsernamesAsStrings1(List<PostgresDbUser> users) {
        return Username.asStrings(users.stream().map(PostgresDbUser::getUsername).collect(Collectors.toList()));
    }

    public static List<String> toUsernamesAsStrings2(List<PostgresDbUserSession> sessions) {
        return Username.asStrings(sessions.stream().map(PostgresDbUserSession::getUsername).collect(Collectors.toList()));
    }

    public static List<String> toAccessTokensAsStrings2(List<PostgresDbUserSession> sessions) {
        return sessions.stream().map(session -> session.getAccessToken().value()).collect(Collectors.toList());
    }
}
