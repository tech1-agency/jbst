package jbst.foundation.configurations;

import jakarta.annotation.PostConstruct;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.services.emails.services.EmailService;
import jbst.foundation.services.emails.services.impl.EmailServiceImpl;
import jbst.foundation.services.emails.services.impl.EmailServiceSlf4j;
import jbst.foundation.services.emails.utilities.EmailUtility;
import jbst.foundation.services.emails.utilities.impl.EmailUtilityImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties({
        JbstProperties.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfigurationEmails {

    // Properties
    private final JbstProperties jbstProperties;

    @PostConstruct
    public void init() {
        this.jbstProperties.getAsyncConfigs().assertProperties(new PropertyId("asyncConfigs"));
        this.jbstProperties.getEmailConfigs().assertProperties(new PropertyId("emailConfigs"));
    }

    @Bean
    @ConditionalOnProperty(value = "jbst.email-configs.enabled", havingValue = "true")
    public JavaMailSender javaMailSender() {
        var emailConfigs = this.jbstProperties.getEmailConfigs();

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
    @ConditionalOnProperty(value = "jbst.email-configs.enabled", havingValue = "true")
    public SpringTemplateEngine springTemplateEngine() {
        var templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(this.htmlTemplateResolver());
        return templateEngine;
    }

    @Bean
    @ConditionalOnProperty(value = "jbst.email-configs.enabled", havingValue = "true")
    public SpringResourceTemplateResolver htmlTemplateResolver() {
        var emailTemplateResolver = new SpringResourceTemplateResolver();
        emailTemplateResolver.setPrefix("classpath:/email-templates/");
        emailTemplateResolver.setSuffix(".html");
        emailTemplateResolver.setTemplateMode(TemplateMode.HTML);
        emailTemplateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return emailTemplateResolver;
    }

    @Bean
    @ConditionalOnProperty(value = "jbst.email-configs.enabled", havingValue = "true")
    public EmailUtility emailUtility() {
        return new EmailUtilityImpl(
                this.javaMailSender()
        );
    }

    @Bean
    @ConditionalOnProperty(value = "jbst.email-configs.enabled", havingValue = "true")
    public EmailService emailService() {
        return new EmailServiceImpl(
                this.javaMailSender(),
                this.springTemplateEngine(),
                this.emailUtility(),
                this.jbstProperties
        );
    }

    @Bean
    @ConditionalOnProperty(value = "jbst.email-configs.enabled", havingValue = "false", matchIfMissing = true)
    public EmailService emailServiceSlf4j() {
        return new EmailServiceSlf4j();
    }
}
