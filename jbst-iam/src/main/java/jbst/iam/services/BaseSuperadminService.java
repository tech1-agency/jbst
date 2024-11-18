package jbst.iam.services;

import jbst.iam.domain.dto.responses.ResponseInvitation;
import jbst.iam.domain.dto.responses.ResponseSuperadminSessionsTable;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.jwt.RequestAccessToken;
import org.springframework.scheduling.annotation.Async;
import jbst.foundation.domain.system.reset_server.ResetServerStatus;

import java.util.List;

public interface BaseSuperadminService {
    // =================================================================================================================
    // Server
    // =================================================================================================================
    ResetServerStatus getResetServerStatus();
    @Async
    void resetServerBy(JwtUser user);

    // =================================================================================================================
    // Invitation Codes
    // =================================================================================================================

    List<ResponseInvitation> findUnused();

    // =================================================================================================================
    // Users Sessions
    // =================================================================================================================

    ResponseSuperadminSessionsTable getSessions(RequestAccessToken requestAccessToken);
}
