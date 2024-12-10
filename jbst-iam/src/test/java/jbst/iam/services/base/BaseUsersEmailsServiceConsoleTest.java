package jbst.iam.services.base;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.domain.properties.configs.EmailConfigs;
import jbst.foundation.domain.properties.configs.MvcConfigs;
import jbst.foundation.domain.properties.configs.SecurityJwtConfigs;
import jbst.foundation.domain.properties.configs.ServerConfigs;
import jbst.foundation.services.emails.services.EmailService;
import jbst.foundation.services.emails.services.impl.EmailServiceImpl;
import jbst.foundation.services.emails.utilities.EmailUtility;
import jbst.foundation.services.emails.utilities.impl.EmailUtilityImpl;
import jbst.iam.domain.functions.FunctionAuthenticationLoginEmail;
import jbst.iam.domain.functions.FunctionEmailConfirmation;
import jbst.iam.domain.functions.FunctionPasswordReset;
import jbst.iam.services.UsersEmailsService;
import jbst.iam.utils.UserEmailUtils;
import jbst.iam.utils.impl.UserEmailUtilsImpl;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;

import static jbst.foundation.utilities.random.RandomUtility.randomStringLetterOrNumbersOnly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class BaseUsersEmailsServiceConsoleTest {

    @Configuration
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        private final ResourceLoader resourceLoader;

        @Bean
        JbstProperties jbstProperties() {
            var jbstProperties = new JbstProperties();
            jbstProperties.setServerConfigs(ServerConfigs.hardcoded());
            jbstProperties.setMvcConfigs(MvcConfigs.hardcoded());
            jbstProperties.setSecurityJwtConfigs(SecurityJwtConfigs.hardcoded());
            jbstProperties.setEmailConfigs(
                    new EmailConfigs(
                            true,
                            "smtp.gmail.com",
                            587,
                            "jbst <?>",
                            Username.of("<?>"),
                            Password.of("<?>")
                    )
            );
            return jbstProperties;
        }

        @Bean
        public JavaMailSender javaMailSender() {
            var emailConfigs = this.jbstProperties().getEmailConfigs();

            var mailSender = new JavaMailSenderImpl();
            mailSender.setHost(emailConfigs.getHost());
            mailSender.setPort(emailConfigs.getPort());

            mailSender.setUsername(emailConfigs.getUsername().value());
            mailSender.setPassword(emailConfigs.getPassword().value());

            var props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "false");

            return mailSender;
        }

        @Bean
        EmailUtility emailUtility() {
            return new EmailUtilityImpl(
                    this.javaMailSender()
            );
        }

        @Bean
        SpringTemplateEngine springTemplateEngine() {
            var templateEngine = new SpringTemplateEngine();
            templateEngine.addTemplateResolver(htmlTemplateResolver());
            return templateEngine;
        }

        @Bean
        SpringResourceTemplateResolver htmlTemplateResolver() {
            var emailTemplateResolver = new SpringResourceTemplateResolver();
            emailTemplateResolver.setPrefix("classpath:/email-templates/");
            emailTemplateResolver.setSuffix(".html");
            emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
            emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return emailTemplateResolver;
        }

        @Bean
        EmailService emailService() {
            return new EmailServiceImpl(
                    this.javaMailSender(),
                    this.springTemplateEngine(),
                    this.emailUtility(),
                    this.jbstProperties()
            );
        }

        @Bean
        UserEmailUtils userEmailUtility() {
            var serverProperties = mock(ServerProperties.class);
            var servlet = mock(ServerProperties.Servlet.class);
            when(servlet.getContextPath()).thenReturn("/api");
            when(serverProperties.getServlet()).thenReturn(servlet);
            return new UserEmailUtilsImpl(
                    this.resourceLoader,
                    this.jbstProperties(),
                    serverProperties
            );
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

    private final UsersEmailsService componentUnderTest;

    private final Username username = Username.hardcoded();
    private final Email email = Email.of("<?>");

    @Disabled
    @Test
    void executeEmailConfirmation() {
        // Arrange
        var function = new FunctionEmailConfirmation(
                this.username,
                this.email,
                randomStringLetterOrNumbersOnly(36)
        );

        // Act
        this.componentUnderTest.executeEmailConfirmation(function);

        // Assert
        // no asserts
    }

    @Disabled
    @Test
    void executePasswordReset() {
        // Arrange
        var function = new FunctionPasswordReset(
                this.username,
                this.email,
                randomStringLetterOrNumbersOnly(36)
        );

        // Act
        this.componentUnderTest.executePasswordReset(function);

        // Assert
        // no asserts
    }

    @Disabled
    @Test
    void executeAuthenticationLogin() {
        // Arrange
        var function = new FunctionAuthenticationLoginEmail(
                this.username,
                this.email,
                UserRequestMetadata.valid()
        );

        // Act
        this.componentUnderTest.executeAuthenticationLogin(function);

        // Assert
        // no asserts
    }
}
