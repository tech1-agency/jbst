package jbst.iam.utils.impl;

import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.http.requests.UserRequestMetadata;
import jbst.foundation.domain.properties.JbstProperties;
import jbst.foundation.utilities.time.LocalDateTimeUtility;
import jbst.iam.domain.enums.AccountAccessMethod;
import jbst.iam.utils.UserEmailUtils;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.time.ZoneOffset;

import static java.time.ZoneOffset.UTC;
import static jbst.foundation.domain.constants.JbstConstants.DateTimeFormatters.DTF11;
import static jbst.foundation.domain.tests.constants.TestsJunitConstants.FIVE_TIMES;
import static jbst.foundation.utilities.random.RandomUtility.randomEnum;
import static jbst.foundation.utilities.time.LocalDateTimeUtility.getTimestamp;
import static jbst.foundation.utilities.time.LocalDateUtility.now;
import static jbst.foundation.utilities.time.TimestampUtility.getCurrentTimestamp;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class UserEmailUtilsImplTest {

    @Configuration
    @Import({
            TestConfigurationPropertiesJbstHardcoded.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        private final ResourceLoader resourceLoader;
        private final JbstProperties jbstProperties;

        @Bean
        UserEmailUtils userEmailUtility() {
            return new UserEmailUtilsImpl(
                    this.resourceLoader,
                    this.jbstProperties
            );
        }
    }

    private final UserEmailUtils componentUnderTest;

    @RepeatedTest(FIVE_TIMES)
    void getSubjectTest() {
        // Act
        var subject = this.componentUnderTest.getSubject("Authentication Login");

        // Assert
        assertThat(subject)
                .startsWith("[jbst] Authentication Login on \"jbst-server\" — ")
                .endsWith(" (UTC)");
        subject = subject.replace("[jbst] Authentication Login on \"jbst-server\" — ", "");
        subject = subject.replace(" (UTC)", "");
        var timestamp = getTimestamp(LocalDateTimeUtility.parse(subject, DTF11), ZoneOffset.UTC);
        assertThat(getCurrentTimestamp() - timestamp).isBetween(0L, 2000L);
    }

    @Test
    void getAuthenticationLoginTemplateNameTest() {
        // Act
        var templateName = this.componentUnderTest.getAuthenticationLoginTemplateName();

        // Assert
        assertThat(templateName).isEqualTo("jbst-account-accessed");
    }

    @Test
    void getSessionRefreshedTemplateNameTest() {
        // Act
        var templateName = this.componentUnderTest.getSessionRefreshedTemplateName();

        // Assert
        assertThat(templateName).isEqualTo("jbst-account-accessed");
    }

    @Test
    void getAuthenticationLoginOrSessionRefreshedVariablesTest() {
        // Arrange
        var username = Username.random();
        var userRequestMetadata = UserRequestMetadata.valid();
        var accountAccessMethod = randomEnum(AccountAccessMethod.class);

        // Act
        var variables = this.componentUnderTest.getAuthenticationLoginOrSessionRefreshedVariables(
                username,
                userRequestMetadata,
                accountAccessMethod
        );

        // Assert
        assertThat(variables)
                .hasSize(7)
                .containsEntry("year", now(UTC).getYear())
                .containsEntry("username", username.value())
                .containsEntry("accessMethod", accountAccessMethod.getValue())
                .containsEntry("where", "🇺🇦 Ukraine, Lviv")
                .containsEntry("what", "Chrome, macOS on Desktop")
                .containsEntry("ipAddress", "127.0.0.1")
                .containsEntry("webclientURL", "http://127.0.0.1:3000");
    }
}
