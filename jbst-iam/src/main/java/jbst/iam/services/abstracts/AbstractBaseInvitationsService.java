package jbst.iam.services.abstracts;

import jbst.iam.domain.dto.requests.RequestNewInvitationParams;
import jbst.iam.domain.dto.responses.ResponseInvitation;
import jbst.iam.domain.dto.responses.ResponseInvitations;
import jbst.iam.domain.identifiers.InvitationId;
import jbst.iam.repositories.InvitationsRepository;
import jbst.iam.services.BaseInvitationsService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.properties.JbstProperties;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractBaseInvitationsService implements BaseInvitationsService {

    // Repositories
    protected final InvitationsRepository invitationsRepository;
    // Properties
    protected final JbstProperties jbstProperties;

    @Override
    public ResponseInvitations findByOwner(Username owner) {
        var invitations = this.invitationsRepository.findResponseCodesByOwner(owner);
        invitations.sort(ResponseInvitation.INVITATION);
        return new ResponseInvitations(
                this.jbstProperties.getSecurityJwtConfigs().getAuthoritiesConfigs().getAvailableAuthorities(),
                invitations
        );
    }

    @Override
    public void save(Username owner, RequestNewInvitationParams request) {
        this.invitationsRepository.saveAs(owner, request);
    }

    @Override
    public void deleteById(InvitationId invitationId) {
        this.invitationsRepository.delete(invitationId);
    }
}
