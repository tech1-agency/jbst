package jbst.iam.startup;

import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.base.Authority;
import jbst.foundation.domain.properties.base.DefaultUsers;
import jbst.foundation.domain.properties.base.Invitations;
import jbst.foundation.domain.properties.configs.SecurityJwtConfigs;
import jbst.foundation.domain.properties.configs.ServerConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.AuthoritiesConfigs;
import jbst.foundation.domain.properties.configs.security.jwt.EssenceConfigs;
import jbst.iam.essence.AbstractEssenceConstructor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class DefaultStartupEventListenerTest {

    private static Stream<Arguments> onStartupTest() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(false, false)
        );
    }

    @Configuration
    static class ContextConfiguration {
        @Bean
        JbstProperties jbstProperties() {
            return mock(JbstProperties.class);
        }

        @Bean
        AbstractEssenceConstructor essenceConstructor() {
            var essenceConstructor = mock(AbstractEssenceConstructor.class);
            setField(essenceConstructor, "jbstProperties", this.jbstProperties());
            return essenceConstructor;
        }

        @Bean
        BaseStartupEventListener baseStartupEventListener() {
            return new DefaultStartupEventListener(
                    this.essenceConstructor(),
                    this.jbstProperties()
            );
        }
    }

    private final AbstractEssenceConstructor essenceConstructor;
    private final JbstProperties jbstProperties;

    private final DefaultStartupEventListener componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.essenceConstructor,
                this.jbstProperties
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.essenceConstructor,
                this.jbstProperties
        );
    }

    @ParameterizedTest
    @MethodSource("onStartupTest")
    void onStartupTest(boolean isDefaultUsersEnabled, boolean invitationsEnabled) {
        // Arrange
        var securityJwtConfigs = new SecurityJwtConfigs(
                new AuthoritiesConfigs(
                        "jbst",
                        Set.of(
                                new Authority("admin"),
                                new Authority("user")
                        )
                ),
                null,
                new EssenceConfigs(
                        new DefaultUsers(
                                isDefaultUsersEnabled,
                                new ArrayList<>()
                        ),
                        new Invitations(
                                invitationsEnabled
                        )
                ),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        when(this.jbstProperties.getServerConfigs()).thenReturn(ServerConfigs.hardcoded());
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);

        // Act
        this.componentUnderTest.onStartup();

        // Assert
        verify(this.jbstProperties, times(2)).getSecurityJwtConfigs();
        if (isDefaultUsersEnabled) {
            verify(this.essenceConstructor).addDefaultUsers();
        }
        if (invitationsEnabled) {
            verify(this.essenceConstructor).addDefaultUsersInvitations();
        }
        reset(
                this.essenceConstructor,
                this.jbstProperties
        );
    }
}
