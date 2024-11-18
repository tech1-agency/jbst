package jbst.iam.assistants.current;

import jakarta.servlet.http.HttpServletRequest;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.dto.responses.ResponseUserSessionsTable;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.security.CurrentClientUser;
import tech1.framework.foundation.domain.base.Username;
import tech1.framework.foundation.domain.exceptions.tokens.AccessTokenNotFoundException;

public interface CurrentSessionAssistant {
    Username getCurrentUsername();
    JwtUser getCurrentJwtUser();
    CurrentClientUser getCurrentClientUser();
    UserSession getCurrentUserSession(HttpServletRequest httpServletRequest) throws AccessTokenNotFoundException;
    ResponseUserSessionsTable getCurrentUserDbSessionsTable(RequestAccessToken requestAccessToken);
}
