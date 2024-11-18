package jbst.iam.resources.base;

import jakarta.servlet.http.HttpServletRequest;
import jbst.iam.assistants.current.CurrentSessionAssistant;
import jbst.iam.domain.db.UserSession;
import jbst.iam.domain.dto.responses.ResponseUserSession2;
import jbst.iam.domain.dto.responses.ResponseUserSessionsTable;
import jbst.iam.domain.identifiers.UserSessionId;
import jbst.iam.domain.jwt.RequestAccessToken;
import jbst.iam.domain.security.CurrentClientUser;
import jbst.iam.services.BaseUsersSessionsService;
import jbst.iam.configurations.TestRunnerResources1;
import jbst.iam.tokens.facade.TokensProvider;
import jbst.iam.validators.BaseUsersSessionsRequestsValidator;
import lombok.RequiredArgsConstructor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jbst.foundation.domain.base.Username;

import java.time.ZoneId;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static jbst.foundation.utilities.random.EntityUtility.list345;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseSecurityUsersSessionsResourceTest extends TestRunnerResources1 {

    // Assistants
    private final CurrentSessionAssistant currentSessionAssistant;
    // Services
    private final BaseUsersSessionsService baseUsersSessionsService;
    // Tokens
    private final TokensProvider tokensProvider;
    // Validators
    private final BaseUsersSessionsRequestsValidator baseUsersSessionsRequestsValidator;

    // Resource
    private final BaseSecurityUsersSessionsResource componentUnderTest;

    @BeforeEach
    void beforeEach() {
        this.standaloneSetupByResourceUnderTest(this.componentUnderTest);
        reset(
                this.currentSessionAssistant,
                this.baseUsersSessionsService,
                this.tokensProvider,
                this.baseUsersSessionsRequestsValidator
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.currentSessionAssistant,
                this.baseUsersSessionsService,
                this.tokensProvider,
                this.baseUsersSessionsRequestsValidator
        );
    }

    @Test
    void getSessionsTableTest() throws Exception {
        // Arrange
        var userSessionsTables = ResponseUserSessionsTable.of(list345(ResponseUserSession2.class));
        var requestAccessToken = RequestAccessToken.random();
        when(this.tokensProvider.readRequestAccessToken(any(HttpServletRequest.class))).thenReturn(requestAccessToken);
        when(this.currentSessionAssistant.getCurrentUserDbSessionsTable(requestAccessToken)).thenReturn(userSessionsTables);

        // Act
        this.mvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions", hasSize(userSessionsTables.sessions().size())))
                .andExpect(jsonPath("$.anyPresent", instanceOf(Boolean.class)))
                .andExpect(jsonPath("$.anyProblem", instanceOf(Boolean.class)));

        // Assert
        verify(this.tokensProvider).readRequestAccessToken(any(HttpServletRequest.class));
        verify(this.currentSessionAssistant).getCurrentUserDbSessionsTable(requestAccessToken);
    }

    @Test
    void getCurrentClientUserCronEnabledTest() throws Exception {
        // Arrange
        var currentClientUser = CurrentClientUser.random();
        var session = UserSession.randomPersistedSession();
        when(this.currentSessionAssistant.getCurrentClientUser()).thenReturn(currentClientUser);
        when(this.currentSessionAssistant.getCurrentUserSession(any(HttpServletRequest.class))).thenReturn(session);

        // Act
        this.mvc.perform(get("/sessions/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(currentClientUser.getUsername().value()))
                .andExpect(jsonPath("$.email").value(currentClientUser.getEmail().value()))
                .andExpect(jsonPath("$.name").value(currentClientUser.getName()))
                .andExpect(jsonPath("$.zoneId").value(currentClientUser.getZoneId().getId()))
                .andExpect(jsonPath("$.authorities").isEmpty())
                .andExpect(jsonPath("$.attributes").isEmpty())
                .andExpect(jsonPath("$.zoneId", new BaseMatcher<String>() {

                    @Override
                    public void describeTo(Description description) {
                        // no actions
                    }

                    @Override
                    public boolean matches(Object o) {
                        var zoneId = ZoneId.of(o.toString());
                        return ZoneId.getAvailableZoneIds().contains(zoneId.getId());
                    }
                }));

        // Assert
        verify(this.currentSessionAssistant).getCurrentClientUser();
        verify(this.currentSessionAssistant).getCurrentUserSession(any(HttpServletRequest.class));
        verify(this.baseUsersSessionsService).renewUserRequestMetadata(eq(session), any(HttpServletRequest.class));
    }

    @Test
    void renewManuallyTest() throws Exception {
        // Arrange
        when(this.currentSessionAssistant.getCurrentUsername()).thenReturn(Username.hardcoded());

        // Act
        this.mvc.perform(
                        post("/sessions/" + UserSessionId.hardcoded() + "/renew/manually")
                )
                .andExpect(status().isOk());

        // Assert
        verify(this.currentSessionAssistant).getCurrentUsername();
        verify(this.baseUsersSessionsService).assertAccess(Username.hardcoded(), UserSessionId.hardcoded());
        verify(this.baseUsersSessionsService).enableUserRequestMetadataRenewManually(UserSessionId.hardcoded());
    }

    @Test
    void deleteByIdTest() throws Exception {
        // Arrange
        when(this.currentSessionAssistant.getCurrentUsername()).thenReturn(Username.hardcoded());

        // Act
        this.mvc.perform(delete("/sessions/" + UserSessionId.hardcoded()))
                .andExpect(status().isOk());

        // Assert
        verify(this.currentSessionAssistant).getCurrentUsername();
        verify(this.baseUsersSessionsService).assertAccess(Username.hardcoded(), UserSessionId.hardcoded());
        verify(this.baseUsersSessionsService).deleteById(UserSessionId.hardcoded());
    }

    @Test
    void deleteAllExceptCurrent() throws Exception {
        // Arrange
        when(this.currentSessionAssistant.getCurrentUsername()).thenReturn(Username.hardcoded());
        when(this.tokensProvider.readRequestAccessToken(any(HttpServletRequest.class))).thenReturn(RequestAccessToken.hardcoded());

        // Act
        this.mvc.perform(delete("/sessions"))
                .andExpect(status().isOk());

        // Assert
        verify(this.currentSessionAssistant).getCurrentUsername();
        verify(this.tokensProvider).readRequestAccessToken(any(HttpServletRequest.class));
        verify(this.baseUsersSessionsService).deleteAllExceptCurrent(Username.hardcoded(), RequestAccessToken.hardcoded());
    }
}
