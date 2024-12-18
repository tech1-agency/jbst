package jbst.iam.services.abstracts;

import jbst.iam.domain.dto.responses.ResponseInvitation;
import jbst.iam.domain.dto.responses.ResponseSuperadminSessionsTable;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.repositories.InvitationsRepository;
import jbst.iam.repositories.UsersSessionsRepository;
import jbst.iam.services.BaseSuperadminService;
import jbst.iam.sessions.SessionRegistry;
import jbst.iam.tasks.superadmin.AbstractSuperAdminResetServerTask;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import jbst.foundation.domain.system.reset_server.ResetServerStatus;
import jbst.foundation.incidents.domain.system.IncidentSystemResetServerCompleted;
import jbst.foundation.incidents.domain.system.IncidentSystemResetServerStarted;
import jbst.foundation.incidents.events.publishers.IncidentPublisher;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractBaseSuperadminService implements BaseSuperadminService {

    // Incidents
    protected final IncidentPublisher incidentPublisher;
    // Sessions
    protected final SessionRegistry sessionRegistry;
    // Repositories
    protected final InvitationsRepository invitationsRepository;
    protected final UsersSessionsRepository usersSessionsRepository;
    // Tasks
    protected final AbstractSuperAdminResetServerTask resetServerTask;

    // =================================================================================================================
    // Server
    // =================================================================================================================
    @Override
    public ResetServerStatus getResetServerStatus() {
        return this.resetServerTask.getStatus();
    }

    @Override
    public void resetServerBy(JwtUser user) {
        this.incidentPublisher.publishResetServerStarted(new IncidentSystemResetServerStarted(user.username()));

        this.resetServerTask.reset(user);

        this.incidentPublisher.publishResetServerCompleted(new IncidentSystemResetServerCompleted(user.username()));
    }

    // =================================================================================================================
    // Invitations
    // =================================================================================================================
    @Override
    public List<ResponseInvitation> findUnused() {
        return this.invitationsRepository.findUnused();
    }

    // =================================================================================================================
    // Users Sessions
    // =================================================================================================================
    @Override
    public ResponseSuperadminSessionsTable getSessions(RequestAccessToken requestAccessToken) {
        var activeAccessTokens = this.sessionRegistry.getActiveSessionsAccessTokens();
        return this.usersSessionsRepository.getSessionsTable(activeAccessTokens, requestAccessToken);
    }
}
