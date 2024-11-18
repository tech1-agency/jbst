package jbst.foundation.configurations;

import jbst.foundation.domain.properties.ApplicationFrameworkProperties;
import jbst.foundation.services.emails.services.EmailService;
import jbst.foundation.services.emails.services.impl.EmailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("SpringBootApplicationProperties")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "tech1.emailConfigs.enabled=true"
)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ApplicationEmails1Test {

    @Configuration
    @Import({
            ConfigurationPropertiesJbstHardcoded.class,
            ApplicationEmails.class
    })
    static class ContextConfiguration {

    }

    private final ApplicationFrameworkProperties applicationFrameworkProperties;

    private final ApplicationEmails componentUnderTest;

    @Test
    void beansTests() {
        // Act
        var methods = Stream.of(this.componentUnderTest.getClass().getMethods())
                .map(Method::getName)
                .collect(Collectors.toList());

        // Assert
        assertThat(methods)
                .contains("javaMailSender")
                .contains("springTemplateEngine")
                .contains("htmlTemplateResolver")
                .contains("emailUtility")
                .contains("emailService")
                .contains("emailServiceSlf4j")
                .hasSize(20);
    }

    @Test
    void javaMailSenderTest() {
        // Act
        var javaMailSender = (JavaMailSenderImpl) this.componentUnderTest.javaMailSender();

        // Assert
        var emailConfigs = this.applicationFrameworkProperties.getEmailConfigs();
        assertThat(javaMailSender.getHost()).isEqualTo(emailConfigs.getHost());
        assertThat(javaMailSender.getPort()).isEqualTo(emailConfigs.getPort());
        assertThat(javaMailSender.getUsername()).isEqualTo(emailConfigs.getUsername().value());
        assertThat(javaMailSender.getPassword()).isEqualTo(emailConfigs.getPassword().value());
        assertThat(javaMailSender.getJavaMailProperties()).hasSize(4);
        assertThat(javaMailSender.getJavaMailProperties()).containsEntry("mail.transport.protocol", "smtp");
        assertThat(javaMailSender.getJavaMailProperties()).containsEntry("mail.smtp.auth", "true");
        assertThat(javaMailSender.getJavaMailProperties()).containsEntry("mail.smtp.starttls.enable", "true");
        assertThat(javaMailSender.getJavaMailProperties()).containsEntry("mail.debug", "false");
    }

    @Test
    void emailServiceTest() {
        // Act
        var incidentClientDefinition = this.componentUnderTest.emailService();

        // Assert
        assertThat(incidentClientDefinition.getClass()).isNotEqualTo(EmailService.class);
        assertThat(incidentClientDefinition.getClass()).isEqualTo(EmailServiceImpl.class);
    }

    @Test
    void emailServiceSlf4jTest() {
        // Act + Assert
        assertThatThrownBy(this.componentUnderTest::emailServiceSlf4j)
                .isInstanceOf(NoSuchBeanDefinitionException.class)
                .hasMessage("No bean named 'emailServiceSlf4j' available");
    }
}
