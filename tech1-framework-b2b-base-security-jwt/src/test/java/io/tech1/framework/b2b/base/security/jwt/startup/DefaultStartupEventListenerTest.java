package io.tech1.framework.b2b.base.security.jwt.startup;

import io.tech1.framework.b2b.base.security.jwt.essense.AbstractEssenceConstructor;
import io.tech1.framework.domain.properties.base.Authority;
import io.tech1.framework.domain.properties.base.DefaultUsers;
import io.tech1.framework.domain.properties.base.InvitationCodes;
import io.tech1.framework.domain.properties.configs.SecurityJwtConfigs;
import io.tech1.framework.domain.properties.configs.security.jwt.AuthoritiesConfigs;
import io.tech1.framework.domain.properties.configs.security.jwt.EssenceConfigs;
import io.tech1.framework.properties.ApplicationFrameworkProperties;
import io.tech1.framework.utilities.environment.EnvironmentUtility;
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
        ApplicationFrameworkProperties applicationFrameworkProperties() {
            return mock(ApplicationFrameworkProperties.class);
        }

        @Bean
        AbstractEssenceConstructor essenceConstructor() {
            var essenceConstructor = mock(AbstractEssenceConstructor.class);
            setField(essenceConstructor, "applicationFrameworkProperties", this.applicationFrameworkProperties());
            return essenceConstructor;
        }

        @Bean
        EnvironmentUtility environmentUtility() {
            return mock(EnvironmentUtility.class);
        }

        @Bean
        BaseStartupEventListener baseStartupEventListener() {
            return new DefaultStartupEventListener(
                    this.essenceConstructor(),
                    this.environmentUtility(),
                    this.applicationFrameworkProperties()
            );
        }
    }

    private final AbstractEssenceConstructor essenceConstructor;
    private final EnvironmentUtility environmentUtility;
    private final ApplicationFrameworkProperties applicationFrameworkProperties;

    private final DefaultStartupEventListener componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.essenceConstructor,
                this.environmentUtility,
                this.applicationFrameworkProperties
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.essenceConstructor,
                this.environmentUtility,
                this.applicationFrameworkProperties
        );
    }

    @ParameterizedTest
    @MethodSource("onStartupTest")
    void onStartupTest(boolean isDefaultUsersEnabled, boolean isInvitationCodesEnabled) {
        // Arrange
        SecurityJwtConfigs securityJwtConfigs = SecurityJwtConfigs.of(
                AuthoritiesConfigs.of(
                        "io.tech1",
                        Set.of(
                                Authority.of("admin"),
                                Authority.of("user")
                        )
                ),
                null,
                EssenceConfigs.of(
                        DefaultUsers.of(
                                isDefaultUsersEnabled,
                                new ArrayList<>()
                        ),
                        InvitationCodes.of(
                                isInvitationCodesEnabled
                        )
                ),
                null,
                null,
                null,
                null,
                null
        );
        when(this.applicationFrameworkProperties.getSecurityJwtConfigs()).thenReturn(securityJwtConfigs);

        // Act
        this.componentUnderTest.onStartup();

        // Assert
        verify(this.environmentUtility).verifyProfilesConfiguration();
        verify(this.applicationFrameworkProperties, times(2)).getSecurityJwtConfigs();
        if (isDefaultUsersEnabled) {
            verify(this.essenceConstructor).addDefaultUsers();
        }
        if (isInvitationCodesEnabled) {
            verify(this.essenceConstructor).addDefaultUsersInvitationCodes();
        }
        reset(
                this.essenceConstructor,
                this.environmentUtility,
                this.applicationFrameworkProperties
        );
    }
}