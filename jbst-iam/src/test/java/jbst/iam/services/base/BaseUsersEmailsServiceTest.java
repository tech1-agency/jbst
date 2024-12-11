package jbst.iam.services.base;

import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.configs.SecurityJwtConfigs;
import jbst.foundation.services.emails.domain.EmailHTML;
import jbst.foundation.services.emails.services.EmailService;
import jbst.iam.domain.functions.FunctionAccountAccessed;
import jbst.iam.domain.functions.FunctionEmailConfirmation;
import jbst.iam.domain.functions.FunctionPasswordReset;
import jbst.iam.services.UsersEmailsService;
import jbst.iam.utils.UserEmailUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static jbst.iam.domain.enums.AccountAccessMethod.SECURITY_TOKEN;
import static jbst.iam.domain.enums.AccountAccessMethod.USERNAME_PASSWORD;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseUsersEmailsServiceTest {

    @Configuration
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        @Bean
        EmailService emailService() {
            return mock(EmailService.class);
        }

        @Bean
        UserEmailUtils userEmailUtility() {
            return mock(UserEmailUtils.class);
        }

        @Bean
        JbstProperties jbstProperties() {
            return mock(JbstProperties.class);
        }

        @Bean
        public UsersEmailsService userEmailService() {
            return new BaseUsersEmailsService(
                    this.emailService(),
                    this.userEmailUtility(),
                    this.jbstProperties()
            );
        }
    }

    // Services
    private final EmailService emailService;
    // Utilities
    private final UserEmailUtils userEmailUtils;
    // Properties
    private final JbstProperties jbstProperties;

    private final UsersEmailsService componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.emailService,
                this.userEmailUtils,
                this.jbstProperties
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.emailService,
                this.userEmailUtils,
                this.jbstProperties
        );
    }

    @Test
    void executeEmailConfirmation() {
        // Arrange
        var function = FunctionEmailConfirmation.hardcoded();
        when(this.userEmailUtils.getEmailConfirmationHTML(function)).thenReturn(EmailHTML.hardcoded());

        // Act
        this.componentUnderTest.executeEmailConfirmation(function);

        // Assert
        verify(this.userEmailUtils).getEmailConfirmationHTML(function);
        verify(this.emailService).sendHTML(EmailHTML.hardcoded());
    }

    @Test
    void executePasswordReset() {
        // Arrange
        var function = FunctionPasswordReset.hardcoded();
        when(this.userEmailUtils.getPasswordResetHTML(function)).thenReturn(EmailHTML.hardcoded());

        // Act
        this.componentUnderTest.executePasswordReset(function);

        // Assert
        verify(this.userEmailUtils).getPasswordResetHTML(function);
        verify(this.emailService).sendHTML(EmailHTML.hardcoded());
    }

    @Test
    void executeAuthenticationLoginDisabled() {
        // Arrange
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.disabledUsersEmailsConfigs());

        // Act
        this.componentUnderTest.executeAuthenticationLogin(FunctionAccountAccessed.hardcoded(USERNAME_PASSWORD));

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void executeAuthenticationLogin() {
        // Arrange
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.hardcoded());
        when(this.userEmailUtils.getAccountAccessedHTML(FunctionAccountAccessed.hardcoded(USERNAME_PASSWORD))).thenReturn(EmailHTML.hardcoded());

        // Act
        this.componentUnderTest.executeAuthenticationLogin(FunctionAccountAccessed.hardcoded(USERNAME_PASSWORD));

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.userEmailUtils).getAccountAccessedHTML(FunctionAccountAccessed.hardcoded(USERNAME_PASSWORD));
        verify(this.emailService).sendHTML(EmailHTML.hardcoded());
    }

    @Test
    void executeSessionRefreshedDisabled() {
        // Arrange
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.disabledUsersEmailsConfigs());

        // Act
        this.componentUnderTest.executeSessionRefreshed(FunctionAccountAccessed.hardcoded(SECURITY_TOKEN));

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
    }

    @Test
    void executeSessionRefreshed() {
        // Arrange
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.hardcoded());
        when(this.userEmailUtils.getAccountAccessedHTML(FunctionAccountAccessed.hardcoded(SECURITY_TOKEN))).thenReturn(EmailHTML.hardcoded());

        // Act
        this.componentUnderTest.executeSessionRefreshed(FunctionAccountAccessed.hardcoded(SECURITY_TOKEN));

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.userEmailUtils).getAccountAccessedHTML(FunctionAccountAccessed.hardcoded(SECURITY_TOKEN));
        verify(this.emailService).sendHTML(EmailHTML.hardcoded());
    }
}
