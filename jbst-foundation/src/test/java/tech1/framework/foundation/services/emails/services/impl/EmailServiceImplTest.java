package tech1.framework.foundation.services.emails.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import tech1.framework.foundation.domain.base.Email;
import tech1.framework.foundation.domain.constants.JbsConstants;
import tech1.framework.foundation.domain.properties.ApplicationFrameworkProperties;
import tech1.framework.foundation.domain.properties.configs.EmailConfigs;
import tech1.framework.foundation.domain.tuples.Tuple2;
import tech1.framework.foundation.services.emails.domain.EmailHTML;
import tech1.framework.foundation.services.emails.domain.EmailPlainAttachment;
import tech1.framework.foundation.services.emails.services.EmailService;
import tech1.framework.foundation.services.emails.utilities.EmailUtility;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static jakarta.mail.Message.RecipientType.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static tech1.framework.foundation.utilities.random.EntityUtility.entity;
import static tech1.framework.foundation.utilities.random.RandomUtility.randomString;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class EmailServiceImplTest {

    @Configuration
    static class ContextConfiguration {
        @Bean
        ApplicationFrameworkProperties applicationFrameworkProperties() {
            return mock(ApplicationFrameworkProperties.class);
        }

        @Bean
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
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
            emailTemplateResolver.setPrefix("classpath:/test-email-templates/");
            emailTemplateResolver.setSuffix(".html");
            emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
            emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
            return emailTemplateResolver;
        }

        @Bean
        EmailUtility emailUtility() {
            return mock(EmailUtility.class);
        }

        @Bean
        EmailService emailService() {
            return new EmailServiceImpl(
                    this.javaMailSender(),
                    this.springTemplateEngine(),
                    this.emailUtility(),
                    this.applicationFrameworkProperties()
            );
        }
    }

    // Services
    private final JavaMailSender javaMailSender;
    // Utilities
    private final EmailUtility emailUtility;
    // Properties
    private final ApplicationFrameworkProperties applicationFrameworkProperties;

    private final EmailService componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.javaMailSender,
                this.emailUtility,
                this.applicationFrameworkProperties
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.javaMailSender,
                this.emailUtility,
                this.applicationFrameworkProperties
        );
    }

    @Test
    void mainSendPlainDisabledTest() {
        // Arrange
        var to = Email.random().value();
        var subject = randomString();
        var message = randomString();
        var emailConfigs = EmailConfigs.disabled();
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);

        // Act
        this.componentUnderTest.sendPlain(new String[] { to }, subject, message);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
    }

    @Test
    void mainSendPlainEnabledTest() {
        // Arrange
        var to = Email.random().value();
        var from = Email.random().value();
        var subject = randomString();
        var message = randomString();
        var emailConfigs = EmailConfigs.enabled(from);
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);

        // Act
        this.componentUnderTest.sendPlain(new String[] { to }, subject, message);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
        var mailMessageAC = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(this.javaMailSender).send(mailMessageAC.capture());
        var simpleMailMessage = mailMessageAC.getValue();
        assertThat(simpleMailMessage.getTo()).isEqualTo(new String[] { to });
        assertThat(simpleMailMessage.getSubject()).isEqualTo(subject);
        assertThat(simpleMailMessage.getText()).isEqualTo(message);
        assertThat(simpleMailMessage.getFrom()).isEqualTo(from);
    }

    @Test
    void listSendPlainEnabledTest() {
        // Arrange
        var to = Email.random().value();
        var from = Email.random().value();
        var subject = randomString();
        var message = randomString();
        var emailConfigs = EmailConfigs.enabled(from);
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);

        // Act
        this.componentUnderTest.sendPlain(List.of(to), subject, message);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
        var mailMessageAC = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(this.javaMailSender).send(mailMessageAC.capture());
        var simpleMailMessage = mailMessageAC.getValue();
        assertThat(simpleMailMessage.getTo()).isEqualTo(new String[] { to });
        assertThat(simpleMailMessage.getSubject()).isEqualTo(subject);
        assertThat(simpleMailMessage.getText()).isEqualTo(message);
        assertThat(simpleMailMessage.getFrom()).isEqualTo(from);
    }

    @Test
    void setSendPlainEnabledTest() {
        // Arrange
        var to = Email.random().value();
        var from = Email.random().value();
        var subject = randomString();
        var message = randomString();
        var emailConfigs = EmailConfigs.enabled(from);
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);

        // Act
        this.componentUnderTest.sendPlain(Set.of(to), subject, message);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
        var mailMessageAC = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(this.javaMailSender).send(mailMessageAC.capture());
        var simpleMailMessage = mailMessageAC.getValue();
        assertThat(simpleMailMessage.getTo()).isEqualTo(new String[] { to });
        assertThat(simpleMailMessage.getSubject()).isEqualTo(subject);
        assertThat(simpleMailMessage.getText()).isEqualTo(message);
        assertThat(simpleMailMessage.getFrom()).isEqualTo(from);
    }

    @Test
    void sendPlainAttachmentDisabledTest() {
        // Arrange
        var emailPlainAttachment = entity(EmailPlainAttachment.class);
        var emailConfigs = EmailConfigs.disabled();
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);

        // Act
        this.componentUnderTest.sendPlainAttachment(emailPlainAttachment);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
    }

    @Test
    void sendPlainAttachmentEnabledExceptionTest() throws MessagingException {
        // Arrange
        var emailPlainAttachment = entity(EmailPlainAttachment.class);
        var from = Email.random().value();
        var emailConfigs = EmailConfigs.enabled(from);
        var mimeMessage = mock(MimeMessage.class);
        doThrow(new MessagingException()).when(mimeMessage).setFrom(from);
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);
        when(this.javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        this.componentUnderTest.sendPlainAttachment(emailPlainAttachment);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
        verify(this.javaMailSender).createMimeMessage();
    }

    @Test
    void sendPlainAttachmentEnabledTest() throws MessagingException, IOException {
        // Arrange
        var emailPlainAttachment = new EmailPlainAttachment(
                Set.of(
                        "test1@" + JbsConstants.Domains.HARDCODED,
                        "test2@" + JbsConstants.Domains.HARDCODED
                ),
                "subject1",
                "message1",
                "attachment-file-name1",
                "attachment-message1"
        );
        var from = Email.random().value();
        var emailConfigs = EmailConfigs.enabled(from);
        var mimeMessage = mock(MimeMessage.class);
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);
        when(this.javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        this.componentUnderTest.sendPlainAttachment(emailPlainAttachment);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
        verify(this.javaMailSender).createMimeMessage();
        verify(mimeMessage).setFrom(from);
        verify(mimeMessage).setSubject("subject1");
        verify(mimeMessage).addRecipients(TO, "test1@" + JbsConstants.Domains.HARDCODED);
        verify(mimeMessage).addRecipients(TO, "test2@" + JbsConstants.Domains.HARDCODED);
        var mimeMultipartAC = ArgumentCaptor.forClass(MimeMultipart.class);
        verify(mimeMessage).setContent(mimeMultipartAC.capture());
        var multipart = mimeMultipartAC.getValue();
        assertThat(multipart.getCount()).isEqualTo(2);
        assertThat(multipart.getBodyPart(0).getContent()).isEqualTo("message1");
        assertThat(multipart.getBodyPart(1).getContent()).isEqualTo("attachment-message1");
        assertThat(multipart.getBodyPart(1).getFileName()).isEqualTo("attachment-file-name1");
        verify(this.javaMailSender).send(any(MimeMessage.class));
        verifyNoMoreInteractions(
                mimeMessage
        );
    }

    @Test
    void sendHTMLDisabledTest() {
        // Arrange
        var emailHTML = entity(EmailHTML.class);
        var emailConfigs = EmailConfigs.disabled();
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);

        // Act
        this.componentUnderTest.sendHTML(emailHTML);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
    }

    @Test
    void sendHTMLEnabledExceptionTest() throws MessagingException {
        // Arrange
        var from = Email.random().value();
        var emailHTML = entity(EmailHTML.class);
        var emailConfigs = EmailConfigs.enabled(from);
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);
        when(this.emailUtility.getMimeMessageTuple2()).thenThrow(new MessagingException());

        // Act
        this.componentUnderTest.sendHTML(emailHTML);

        // Assert
        verify(this.applicationFrameworkProperties).getEmailConfigs();
        verify(this.emailUtility).getMimeMessageTuple2();
    }

    @Test
    void sendHTMLEnabledTest() throws MessagingException {
        // Arrange
        var from = Email.random().value();
        Map<String, Object> templateVariables = Map.of(
                "param1", "key2",
                "param2", 2L
        );
        var emailHTML = new EmailHTML(
                Set.of(
                        "tests@" + JbsConstants.Domains.HARDCODED
                ),
                "subject1",
                "template1",
                templateVariables
        );
        var emailConfigs = EmailConfigs.enabled(from);
        when(this.applicationFrameworkProperties.getEmailConfigs()).thenReturn(emailConfigs);
        var message = mock(MimeMessage.class);
        var mimeMessageHelper = mock(MimeMessageHelper.class);
        when(this.emailUtility.getMimeMessageTuple2()).thenReturn(new Tuple2<>(message, mimeMessageHelper));

        // Act
        this.componentUnderTest.sendHTML(emailHTML);

        // Assert
        verify(mimeMessageHelper).setFrom(from);
        verify(mimeMessageHelper).setTo(new String[] { "tests@" + JbsConstants.Domains.HARDCODED });
        verify(mimeMessageHelper).setSubject("subject1");
        var html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                </head>
                <body>
                <p>
                  Param1: <span >key2</span>
                </p>
                <p>
                  Param2: <span >2</span>
                </p>
                </body>
                </html>
                """;
        verify(mimeMessageHelper).setText(html, true);
        verify(this.applicationFrameworkProperties).getEmailConfigs();
        verify(this.emailUtility).getMimeMessageTuple2();
        verify(this.javaMailSender).send(any(MimeMessage.class));
        verifyNoMoreInteractions(
                message,
                mimeMessageHelper
        );
    }
}