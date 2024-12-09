package jbst.iam.services.abstracts;

import jbst.foundation.domain.base.Email;
import jbst.foundation.domain.base.Password;
import jbst.iam.domain.db.UserToken;
import jbst.iam.domain.dto.requests.RequestUserChangePasswordBasic;
import jbst.iam.domain.dto.requests.RequestUserResetPassword;
import jbst.iam.domain.dto.requests.RequestUserUpdate1;
import jbst.iam.domain.dto.requests.RequestUserUpdate2;
import jbst.iam.domain.jwt.JwtUser;
import jbst.iam.repositories.UsersRepository;
import jbst.iam.repositories.UsersTokensRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static jbst.foundation.domain.tests.constants.TestsJunitConstants.FIVE_TIMES;

@ExtendWith({ SpringExtension.class })
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class AbstractBaseUsersServiceTest {
    @Configuration
    static class ContextConfiguration {
        @Bean
        UsersTokensRepository usersTokensRepository() {
            return mock(UsersTokensRepository.class);
        }

        @Bean
        UsersRepository userRepository() {
            return mock(UsersRepository.class);
        }

        @Bean
        BCryptPasswordEncoder bCryptPasswordEncoder() {
            return new BCryptPasswordEncoder(11);
        }

        @Bean
        AbstractBaseUsersService baseUserService() {
            return new AbstractBaseUsersService(
                    this.usersTokensRepository(),
                    this.userRepository(),
                    this.bCryptPasswordEncoder()
            ) {};
        }
    }

    private final UsersTokensRepository usersTokensRepository;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final AbstractBaseUsersService componentUnderTest;

    @BeforeEach
    void beforeEach() {
        reset(
                this.usersTokensRepository,
                this.usersRepository
        );
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
                this.usersTokensRepository,
                this.usersRepository
        );
    }

    @Test
    void findByEmailTest() {
        // Arrange
        var email = Email.hardcoded();
        var user = JwtUser.hardcoded();
        when(this.usersRepository.findByEmailAsJwtUserOrNull(email)).thenReturn(user);

        // Act
        var actual = this.componentUnderTest.findByEmail(email);

        // Assert
        assertThat(actual).isEqualTo(user);
        verify(this.usersRepository).findByEmailAsJwtUserOrNull(email);
    }

    @Test
    void updateUser1() {
        // Arrange
        var request = RequestUserUpdate1.hardcoded();
        var user = JwtUser.hardcoded();
        var userAC = ArgumentCaptor.forClass(JwtUser.class);

        // Act
        this.componentUnderTest.updateUser1(user, request);

        // Assert
        verify(this.usersRepository).saveAs(userAC.capture());
        assertThat(userAC.getValue().username()).isEqualTo(user.username());
        assertThat(userAC.getValue().zoneId()).isEqualTo(request.zoneId());
        assertThat(userAC.getValue().name()).isEqualTo(request.name());
        assertThat(userAC.getValue().email()).isEqualTo(request.email());
        // no verifications on static SecurityContextHolder
    }

    @Test
    void updateUser2() {
        // Arrange
        var request = RequestUserUpdate2.hardcoded();
        var user = JwtUser.hardcoded();
        var userAC = ArgumentCaptor.forClass(JwtUser.class);

        // Act
        this.componentUnderTest.updateUser2(user, request);

        // Assert
        verify(this.usersRepository).saveAs(userAC.capture());
        assertThat(userAC.getValue().username()).isEqualTo(user.username());
        assertThat(userAC.getValue().zoneId()).isEqualTo(request.zoneId());
        assertThat(userAC.getValue().name()).isEqualTo(request.name());
        // no verifications on static SecurityContextHolder
    }

    @RepeatedTest(FIVE_TIMES)
    void changePasswordRequired() {
        // Arrange
        var request = RequestUserChangePasswordBasic.hardcoded();
        var user = JwtUser.random();
        var userAC = ArgumentCaptor.forClass(JwtUser.class);

        // Act
        this.componentUnderTest.changePasswordRequired(user, request);

        // Assert
        verify(this.usersRepository).saveAs(userAC.capture());
        assertThat(userAC.getValue().passwordChangeRequired()).isFalse();
        assertThat(userAC.getValue().username()).isEqualTo(user.username());
        assertThat(
                this.bCryptPasswordEncoder.matches(
                        request.newPassword().value(),
                        userAC.getValue().getPassword()
                )
        ).isTrue();
        // no verifications on static SecurityContextHolder
    }

    @RepeatedTest(FIVE_TIMES)
    void changePassword1() {
        // Arrange
        var request = RequestUserChangePasswordBasic.hardcoded();
        var user = JwtUser.hardcoded();
        var userAC = ArgumentCaptor.forClass(JwtUser.class);

        // Act
        this.componentUnderTest.changePassword1(user, request);

        // Assert
        verify(this.usersRepository).saveAs(userAC.capture());
        assertThat(userAC.getValue().passwordChangeRequired()).isEqualTo(user.passwordChangeRequired());
        assertThat(userAC.getValue().username()).isEqualTo(user.username());
        assertThat(
                this.bCryptPasswordEncoder.matches(
                        request.newPassword().value(),
                        userAC.getValue().getPassword()
                )
        ).isTrue();
        // no verifications on static SecurityContextHolder
    }

    @Test
    void resetPasswordTest() {
        // Arrange
        var request = RequestUserResetPassword.hardcoded();
        var userToken = UserToken.hardcoded();
        when(this.usersTokensRepository.findByValueAsAny(request.token())).thenReturn(userToken);
        var passwordAC = ArgumentCaptor.forClass(Password.class);

        // Act
        this.componentUnderTest.resetPassword(request);

        // Assert
        verify(this.usersTokensRepository).findByValueAsAny(request.token());
        verify(this.usersRepository).resetPassword(eq(userToken.username()), passwordAC.capture());
        verify(this.usersTokensRepository).saveAs(userToken.withUsed(true));
        assertThat(
                this.bCryptPasswordEncoder.matches(
                        request.newPassword().value(),
                        passwordAC.getValue().value()
                )
        ).isTrue();
    }
}
