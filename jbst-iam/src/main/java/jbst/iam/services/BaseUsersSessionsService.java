package jbst.iam.services;

import jakarta.servlet.http.HttpServletRequest;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.events.EventSessionUserRequestMetadataAdd;
import jbst.iam.domain.events.EventSessionUserRequestMetadataRenew;
import jbst.iam.domain.functions.FunctionSessionUserRequestMetadataSave;
import jbst.iam.domain.identifiers.UserSessionId;
import jbst.iam.domain.jwt.JwtAccessToken;
import jbst.iam.domain.jwt.JwtRefreshToken;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.sessions.SessionsExpiredTable;
import jbst.foundation.domain.base.Username;

import java.util.Set;

public interface BaseUsersSessionsService {
    void assertAccess(Username username, UserSessionId sessionId);
    void save(JwtUser user, JwtAccessToken accessToken, JwtRefreshToken refreshToken, HttpServletRequest httpServletRequest);
    void refresh(JwtUser user, UserSession oldSession, JwtAccessToken newAccessToken, JwtRefreshToken newRefreshToken, HttpServletRequest httpServletRequest);
    UserSession saveUserRequestMetadata(EventSessionUserRequestMetadataAdd event);
    void saveUserRequestMetadata(EventSessionUserRequestMetadataRenew event);
    UserSession saveUserRequestMetadata(FunctionSessionUserRequestMetadataSave saveFunction);
    SessionsExpiredTable getExpiredRefreshTokensSessions(Set<Username> usernames);
    void enableUserRequestMetadataRenewCron();
    void enableUserRequestMetadataRenewManually(UserSessionId sessionId);
    void renewUserRequestMetadata(UserSession session, HttpServletRequest httpServletRequest);
    void deleteById(UserSessionId sessionId);
    void deleteAllExceptCurrent(Username username, RequestAccessToken requestAccessToken);
    void deleteAllExceptCurrentAsSuperuser(RequestAccessToken requestAccessToken);
}
