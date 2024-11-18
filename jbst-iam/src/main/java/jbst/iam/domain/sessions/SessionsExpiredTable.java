package jbst.iam.domain.sessions;

import jbst.iam.domain.identifiers.UserSessionId;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.tuples.Tuple3;

import java.util.List;
import java.util.Set;

public record SessionsExpiredTable(
        List<Tuple3<Username, JwtRefreshToken, UserRequestMetadata>> expiredSessions,
        Set<UserSessionId> expiredOrInvalidSessionIds
) {
}
