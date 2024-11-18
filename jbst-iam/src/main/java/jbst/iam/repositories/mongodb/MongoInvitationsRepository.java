package jbst.iam.repositories.mongodb;

import jbst.iam.domain.db.Invitation;
import jbst.iam.domain.dto.requests.RequestNewInvitationParams;
import jbst.iam.domain.dto.responses.ResponseInvitation;
import jbst.iam.domain.identifiers.InvitationId;
import jbst.iam.repositories.InvitationsRepository;
import jbst.iam.domain.mongodb.MongoDbInvitation;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.tuples.TuplePresence;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.stream.Collectors;

import static jbst.iam.domain.db.Invitation.INVITATION_CODES_UNUSED;
import static jbst.foundation.utilities.spring.SpringAuthoritiesUtility.getSimpleGrantedAuthorities;
import static jbst.foundation.domain.tuples.TuplePresence.present;
import static java.util.Objects.nonNull;

public interface MongoInvitationsRepository extends MongoRepository<MongoDbInvitation, String>, InvitationsRepository {
    // ================================================================================================================
    // Any
    // ================================================================================================================
    default TuplePresence<Invitation> isPresent(InvitationId invitationId) {
        return this.findById(invitationId.value())
                .map(entity -> present(entity.invitation()))
                .orElseGet(TuplePresence::absent);
    }

    default List<ResponseInvitation> findResponseCodesByOwner(Username owner) {
        return this.findByOwner(owner).stream()
                .map(MongoDbInvitation::responseInvitation)
                .collect(Collectors.toList());
    }

    default Invitation findByValueAsAny(String value) {
        var invitation = this.findByValue(value);
        return nonNull(invitation) ? invitation.invitation() : null;
    }

    default List<ResponseInvitation> findUnused() {
        return this.findByInvitedIsNull(INVITATION_CODES_UNUSED).stream()
                .map(MongoDbInvitation::responseInvitation)
                .collect(Collectors.toList());
    }

    long countByOwner(Username username);

    default void delete(InvitationId invitationId) {
        this.deleteById(invitationId.value());
    }

    default InvitationId saveAs(Invitation invitation) {
        var entity = this.save(new MongoDbInvitation(invitation));
        return entity.invitationId();
    }

    default InvitationId saveAs(Username owner, RequestNewInvitationParams request) {
        var invitation = new MongoDbInvitation(
                owner,
                getSimpleGrantedAuthorities(request.authorities())
        );
        var entity = this.save(invitation);
        return entity.invitationId();
    }

    // ================================================================================================================
    // Spring Data
    // ================================================================================================================
    List<MongoDbInvitation> findByOwner(Username username);
    List<MongoDbInvitation> findByInvitedIsNull();
    List<MongoDbInvitation> findByInvitedIsNull(Sort sort);
    List<MongoDbInvitation> findByInvitedIsNotNull();
    List<MongoDbInvitation> findByInvitedIsNotNull(Sort sort);
    MongoDbInvitation findByValue(String value);

    void deleteByInvitedIsNull();
    void deleteByInvitedIsNotNull();
}
