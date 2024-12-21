package jbst.iam.domain.events;

import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.functions.FunctionSessionUserRequestMetadataSave;
import org.jetbrains.annotations.NotNull;
import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.http.requests.IPAddress;
import jbst.foundation.domain.http.requests.UserAgentHeader;
import jbst.foundation.domain.tuples.TupleToggle;
import org.jetbrains.annotations.Nullable;

public record EventSessionUserRequestMetadataAdd(
        @NotNull Username username,
        @Nullable Email email,
        @NotNull UserSession session,
        @NotNull IPAddress clientIpAddr,
        UserAgentHeader userAgentHeader,
        boolean isAuthenticationLoginEndpoint,
        boolean isAuthenticationRefreshTokenEndpoint
) {
    public FunctionSessionUserRequestMetadataSave getSaveFunction() {
        return new FunctionSessionUserRequestMetadataSave(
                this.username,
                this.session,
                this.clientIpAddr,
                this.userAgentHeader,
                TupleToggle.disabled(),
                TupleToggle.disabled()
        );
    }
}
