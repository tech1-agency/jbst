package jbst.iam.validators.abstracts;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Username;
import jbst.foundation.domain.exceptions.tokens.UserTokenValidationException;
import jbst.foundation.domain.time.TimeAmount;
import jbst.foundation.utilities.random.RandomUtility;
import jbst.foundation.utilities.time.TimestampUtility;
import jbst.iam.configurations.TestConfigurationValidators;
import jbst.iam.domain.db.UserEmailDetails;
import jbst.iam.domain.db.UserToken;
import jbst.iam.domain.enums.UserTokenType;
import jbst.iam.domain.identifiers.TokenId;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.repositories.UsersTokensRepository;
import jbst.iam.validators.BaseUsersTokensRequestsValidator;
import jbst.iam.validators.abtracts.AbstractBaseUsersTokensRequestsValidator;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AbstractBaseUsersTokensRequestsValidatorTest {

    private static Stream<Arguments> validateExecuteConfirmEmailTest() {
        return Stream.of(
                Arguments.of(
                        JwtUser.hardcoded(Email.hardcoded(), UserEmailDetails.required()),
                        null
                ),
                Arguments.of(
                        JwtUser.hardcoded(null, UserEmailDetails.unnecessary()),
                        new IllegalArgumentException("User email already confirmed")
                ),
                Arguments.of(
                        JwtUser.hardcoded(null, UserEmailDetails.confirmed()),
                        new IllegalArgumentException("User email already confirmed")
                ),
                Arguments.of(
                        JwtUser.hardcoded(null, UserEmailDetails.required()),
                        new IllegalArgumentException("User email is missing")
                ),
                Arguments.of(
                        JwtUser.hardcoded(Email.hardcoded(), UserEmailDetails.unnecessary()),
                        new IllegalArgumentException("User email already confirmed")
                ),
                Arguments.of(
                        JwtUser.hardcoded(Email.hardcoded(), UserEmailDetails.confirmed()),
                        new IllegalArgumentException("User email already confirmed")
                )
        );
    }

    private static Stream<Arguments> validateEmailConfirmationTokenTest() {
        var oneDay = new TimeAmount(24, ChronoUnit.HOURS);
        var expiredTimestamp = TimestampUtility.getPastRange(oneDay).from();
        var validTimestamp = TimestampUtility.getFutureRange(oneDay).to();
        return Stream.of(
                Arguments.of(
                        null,
                        UserTokenValidationException.notFound()
                ),
                Arguments.of(
                        new UserToken(
                                TokenId.random(),
                                Username.random(),
                                RandomUtility.randomStringLetterOrNumbersOnly(36),
                                UserTokenType.EMAIL_CONFIRMATION,
                                validTimestamp,
                                true
                        ),
                        UserTokenValidationException.used()
                ),
                Arguments.of(
                        new UserToken(
                                TokenId.random(),
                                Username.random(),
                                RandomUtility.randomStringLetterOrNumbersOnly(36),
                                UserTokenType.EMAIL_CONFIRMATION,
                                expiredTimestamp,
                                false
                        ),
                        UserTokenValidationException.expired()
                ),
                Arguments.of(
                        new UserToken(
                                TokenId.random(),
                                Username.random(),
                                RandomUtility.randomStringLetterOrNumbersOnly(36),
                                UserTokenType.PASSWORD_RESET,
                                validTimestamp,
                                false
                        ),
                        UserTokenValidationException.invalidType()
                ),
                Arguments.of(
                        new UserToken(
                                TokenId.random(),
                                Username.random(),
                                RandomUtility.randomStringLetterOrNumbersOnly(36),
                                UserTokenType.EMAIL_CONFIRMATION,
                                validTimestamp,
                                false
                        ),
                        null
                )
        );
    }

    @Configuration
    @Import({
            TestConfigurationValidators.class
    })
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    static class ContextConfiguration {
        private final UsersTokensRepository usersTokensRepository;

        @Bean
        BaseUsersTokensRequestsValidator baseUsersEmailsTokensRequestsValidator() {
            return new AbstractBaseUsersTokensRequestsValidator(
                    this.usersTokensRepository
            ) {};
        }

    }

    private final UsersTokensRepository usersTokensRepository;

    private final BaseUsersTokensRequestsValidator componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.usersTokensRepository
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.usersTokensRepository
        );
    }

    @ParameterizedTest
    @MethodSource("validateExecuteConfirmEmailTest")
    void validateExecuteConfirmEmailTest(JwtUser user, IllegalArgumentException expected) {
        // Act
        var actual = catchThrowable(() -> this.componentUnderTest.validateExecuteConfirmEmail(user));

        if (nonNull(expected)) {
            assertThat(actual)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(expected.getMessage());
        } else {
            assertThat(actual).isNull();
        }
    }

    @ParameterizedTest
    @MethodSource("validateEmailConfirmationTokenTest")
    void validateEmailConfirmationTokenTest(
            UserToken userToken,
            UserTokenValidationException expected
    ) {
        // Arrange
        var token = RandomUtility.randomStringLetterOrNumbersOnly(36);
        when(this.usersTokensRepository.findByValueAsAny(token)).thenReturn(userToken);

        // Act
        var actual = catchThrowable(() -> this.componentUnderTest.validateEmailConfirmationToken(token));

        // Assert
        verify(this.usersTokensRepository).findByValueAsAny(token);
        if (nonNull(expected)) {
            assertThat(actual)
                    .isInstanceOf(UserTokenValidationException.class)
                    .hasMessage(expected.getMessage());
        } else {
            assertThat(actual).isNull();
        }
    }

}