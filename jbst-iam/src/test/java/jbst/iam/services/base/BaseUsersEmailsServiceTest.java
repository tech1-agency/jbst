package jbst.iam.services.base;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.configs.SecurityJwtConfigs;
import jbst.foundation.services.emails.domain.EmailHTML;
import jbst.foundation.services.emails.services.EmailService;
import jbst.iam.domain.enums.AccountAccessMethod;
import jbst.iam.domain.functions.FunctionAuthenticationLoginEmail;
import jbst.iam.domain.functions.FunctionConfirmEmail;
import jbst.iam.domain.functions.FunctionResetPassword;
import jbst.iam.domain.functions.FunctionSessionRefreshedEmail;
import jbst.iam.services.UsersEmailsService;
import jbst.iam.utils.UserEmailUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Map;

import static jbst.foundation.utilities.random.RandomUtility.randomString;
import static org.assertj.core.api.Assertions.assertThat;
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
    void executeConfirmEmailTest() {
        // Arrange
        var function = FunctionConfirmEmail.hardcoded();
        var subject = randomString();
        var variables = Map.of(
                randomString(), new Object(),
                randomString(), new Object(),
                randomString(), new Object()
        );
        when(this.userEmailUtils.getSubject("Confirm Email")).thenReturn(subject);
        when(this.userEmailUtils.getConfirmEmailTemplateName()).thenReturn("jbst-confirm-email");
        when(this.userEmailUtils.getConfirmEmailVariables(function.username(), function.token())).thenReturn(variables);

        // Act
        this.componentUnderTest.executeConfirmEmail(function);

        // Assert
        verify(this.userEmailUtils).getSubject("Confirm Email");
        verify(this.userEmailUtils).getConfirmEmailTemplateName();
        verify(this.userEmailUtils).getConfirmEmailVariables(function.username(), function.token());
        var emailHTMLAC = ArgumentCaptor.forClass(EmailHTML.class);
        verify(this.emailService).sendHTML(emailHTMLAC.capture());
        var emailHTML = emailHTMLAC.getValue();
        assertThat(emailHTML.to()).hasSize(1);
        assertThat(emailHTML.to()).containsOnly(function.email().value());
        assertThat(emailHTML.subject()).isEqualTo(subject);
        assertThat(emailHTML.templateName()).isEqualTo("jbst-confirm-email");
        assertThat(emailHTML.templateVariables()).isEqualTo(variables);
    }

    @Test
    void executeResetPasswordTest() {
        // Arrange
        var function = FunctionResetPassword.hardcoded();
        var subject = randomString();
        var variables = Map.of(
                randomString(), new Object(),
                randomString(), new Object(),
                randomString(), new Object()
        );
        when(this.userEmailUtils.getSubject("Reset Password")).thenReturn(subject);
        when(this.userEmailUtils.getResetPasswordTemplateName()).thenReturn("jbst-reset-password");
        when(this.userEmailUtils.getResetPasswordVariables(function.username(), function.token())).thenReturn(variables);

        // Act
        this.componentUnderTest.executeResetPassword(function);

        // Assert
        verify(this.userEmailUtils).getSubject("Reset Password");
        verify(this.userEmailUtils).getResetPasswordTemplateName();
        verify(this.userEmailUtils).getResetPasswordVariables(function.username(), function.token());
        var emailHTMLAC = ArgumentCaptor.forClass(EmailHTML.class);
        verify(this.emailService).sendHTML(emailHTMLAC.capture());
        var emailHTML = emailHTMLAC.getValue();
        assertThat(emailHTML.to()).hasSize(1);
        assertThat(emailHTML.to()).containsOnly(function.email().value());
        assertThat(emailHTML.subject()).isEqualTo(subject);
        assertThat(emailHTML.templateName()).isEqualTo("jbst-reset-password");
        assertThat(emailHTML.templateVariables()).isEqualTo(variables);
    }

    @Test
    void noExecutionNullEmailAndEnabledEmailTest() {
        // Arrange
        var function = new FunctionAuthenticationLoginEmail(
                Username.random(),
                null,
                UserRequestMetadata.random()
        );
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.hardcoded());

        // Act
        this.componentUnderTest.executeAuthenticationLogin(function);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.userEmailUtils).getAuthenticationLoginTemplateName();
    }

    @Test
    void noExecutionNullEmailAndDisabledEmailTest() {
        // Arrange
        var function = new FunctionAuthenticationLoginEmail(
                Username.random(),
                null,
                UserRequestMetadata.random()
        );
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.disabledUsersEmailsConfigs());

        // Act
        this.componentUnderTest.executeAuthenticationLogin(function);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.userEmailUtils).getAuthenticationLoginTemplateName();
    }

    @Test
    void noExecutionNotNullEmailAndDisabledEmailTest() {
        // Arrange
        var function = new FunctionAuthenticationLoginEmail(
                Username.random(),
                Email.random(),
                UserRequestMetadata.random()
        );
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.disabledUsersEmailsConfigs());

        // Act
        this.componentUnderTest.executeAuthenticationLogin(function);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.userEmailUtils).getAuthenticationLoginTemplateName();
    }

    @Test
    void executeAuthenticationLoginTest() {
        // Arrange
        var username = Username.random();
        var email = Email.random();
        var userRequestMetadata = UserRequestMetadata.random();
        var function = new FunctionAuthenticationLoginEmail(
                username,
                email,
                userRequestMetadata
        );
        var subject = randomString();
        var variables = Map.of(
                randomString(), new Object(),
                randomString(), new Object(),
                randomString(), new Object()
        );
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.hardcoded());
        when(this.userEmailUtils.getAuthenticationLoginTemplateName()).thenReturn("jbst-account-accessed");
        when(this.userEmailUtils.getSubject("Account Accessed")).thenReturn(subject);
        when(this.userEmailUtils.getAuthenticationLoginOrSessionRefreshedVariables(
                username,
                userRequestMetadata,
                AccountAccessMethod.USERNAME_PASSWORD
        )).thenReturn(variables);

        // Act
        this.componentUnderTest.executeAuthenticationLogin(function);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.userEmailUtils).getAuthenticationLoginTemplateName();
        verify(this.userEmailUtils).getSubject("Account Accessed");
        verify(this.userEmailUtils).getAuthenticationLoginOrSessionRefreshedVariables(
                username,
                userRequestMetadata,
                AccountAccessMethod.USERNAME_PASSWORD
        );
        var emailHTMLAC = ArgumentCaptor.forClass(EmailHTML.class);
        verify(this.emailService).sendHTML(emailHTMLAC.capture());
        var emailHTML = emailHTMLAC.getValue();
        assertThat(emailHTML.to()).hasSize(1);
        assertThat(emailHTML.to().stream().iterator().next()).isEqualTo(email.value());
        assertThat(emailHTML.subject()).isEqualTo(subject);
        assertThat(emailHTML.templateName()).isEqualTo("jbst-account-accessed");
        assertThat(emailHTML.templateVariables()).isEqualTo(variables);
    }

    @Test
    void executeSessionRefreshedTest() {
        // Arrange
        var username = Username.random();
        var email = Email.random();
        var userRequestMetadata = UserRequestMetadata.random();
        var function = new FunctionSessionRefreshedEmail(
                username,
                email,
                userRequestMetadata
        );
        var subject = randomString();
        var variables = Map.of(
                randomString(), new Object(),
                randomString(), new Object(),
                randomString(), new Object()
        );
        when(this.jbstProperties.getSecurityJwtConfigs()).thenReturn(SecurityJwtConfigs.hardcoded());
        when(this.userEmailUtils.getSessionRefreshedTemplateName()).thenReturn("jbst-account-accessed");
        when(this.userEmailUtils.getSubject("Account Accessed")).thenReturn(subject);
        when(this.userEmailUtils.getAuthenticationLoginOrSessionRefreshedVariables(
                username,
                userRequestMetadata,
                AccountAccessMethod.SECURITY_TOKEN
        )).thenReturn(variables);

        // Act
        this.componentUnderTest.executeSessionRefreshed(function);

        // Assert
        verify(this.jbstProperties).getSecurityJwtConfigs();
        verify(this.userEmailUtils).getSessionRefreshedTemplateName();
        verify(this.userEmailUtils).getSubject("Account Accessed");
        verify(this.userEmailUtils).getAuthenticationLoginOrSessionRefreshedVariables(
                username,
                userRequestMetadata,
                AccountAccessMethod.SECURITY_TOKEN
        );
        var emailHTMLAC = ArgumentCaptor.forClass(EmailHTML.class);
        verify(this.emailService).sendHTML(emailHTMLAC.capture());
        var emailHTML = emailHTMLAC.getValue();
        assertThat(emailHTML.to()).hasSize(1);
        assertThat(emailHTML.to().stream().iterator().next()).isEqualTo(email.value());
        assertThat(emailHTML.subject()).isEqualTo(subject);
        assertThat(emailHTML.templateName()).isEqualTo("jbst-account-accessed");
        assertThat(emailHTML.templateVariables()).isEqualTo(variables);
    }
}
