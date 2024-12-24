package jbst.foundation.domain.properties.configs;

import jbst.foundation.domain.base.AbstractAuthority;
import jbst.foundation.domain.base.PropertyId;
import jbst.foundation.domain.properties.annotations.MandatoryProperty;
import jbst.foundation.domain.properties.annotations.NonMandatoryProperty;
import jbst.foundation.domain.properties.base.Checkbox;
import jbst.foundation.domain.properties.configs.security.jwt.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static jbst.foundation.domain.asserts.Asserts.assertTrueOrThrow;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityJwtConfigs extends AbstractPropertiesConfigs {
    @MandatoryProperty
    private final AuthoritiesConfigs authoritiesConfigs;
    @MandatoryProperty
    private final CookiesConfigs cookiesConfigs;
    @MandatoryProperty
    private final EssenceConfigs essenceConfigs;
    @MandatoryProperty
    private final IncidentsConfigs incidentsConfigs;
    @MandatoryProperty
    private final JwtTokensConfigs jwtTokensConfigs;
    @MandatoryProperty
    private final LoggingConfigs loggingConfigs;
    @MandatoryProperty
    private final SessionConfigs sessionConfigs;
    @MandatoryProperty
    private final UsersEmailsConfigs usersEmailsConfigs;
    @MandatoryProperty
    private final WebsocketsConfigs websocketsConfigs;
    @NonMandatoryProperty
    private final UsersTokensConfigs usersTokensConfigs;

    public static SecurityJwtConfigs hardcoded() {
        return new SecurityJwtConfigs(
                AuthoritiesConfigs.hardcoded(),
                CookiesConfigs.hardcoded(),
                EssenceConfigs.hardcoded(),
                IncidentsConfigs.hardcoded(),
                JwtTokensConfigs.hardcoded(),
                LoggingConfigs.hardcoded(),
                SessionConfigs.hardcoded(),
                UsersEmailsConfigs.hardcoded(),
                WebsocketsConfigs.hardcoded(),
                UsersTokensConfigs.hardcoded()
        );
    }

    public static SecurityJwtConfigs of(LoggingConfigs loggingConfigs) {
        return new SecurityJwtConfigs(
                null,
                null,
                null,
                null,
                null,
                loggingConfigs,
                null,
                null,
                null,
                null
        );
    }

    public static SecurityJwtConfigs of(SessionConfigs sessionConfigs) {
        return new SecurityJwtConfigs(
                null,
                null,
                null,
                null,
                null,
                null,
                sessionConfigs,
                null,
                null,
                null
        );
    }

    public static SecurityJwtConfigs disabledUsersEmailsConfigs() {
        return new SecurityJwtConfigs(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new UsersEmailsConfigs(
                        "[jbst]",
                        Checkbox.disabled(),
                        Checkbox.disabled()
                ),
                null,
                null
        );
    }

    @Override
    public boolean isParentPropertiesNode() {
        return true;
    }

    @Override
    public void assertProperties(PropertyId propertyId) {
        super.assertProperties(propertyId);

        // Requirements: availableAuthorities vs. defaultUsersAuthorities
        var expectedAuthorities = this.authoritiesConfigs.getAllAuthoritiesValues();
        var defaultUsersAuthorities = this.essenceConfigs.getDefaultUsers().getDefaultUsersAuthorities();
        var containsAll = expectedAuthorities.containsAll(defaultUsersAuthorities);
        assertTrueOrThrow(containsAll, "Please verify `defaultUsers.users.authorities`. Configuration provide unauthorized authority");

        // Requirements: availableAuthorities vs. required enum values
        var authorityClasses = this.getAbstractAuthorityClasses(this.authoritiesConfigs.getPackageName());
        var size = authorityClasses.size();
        assertTrueOrThrow(size == 1, "Please verify AbstractAuthority.class has only one sub enum. Found: `" + size + "`");
        var authorityClass = authorityClasses.iterator().next();
        Set<String> actualAuthorities = new HashSet<>();
        var abstractAuthorityClass = AbstractAuthority.class;
        var jbstAuthorities = Stream.of(abstractAuthorityClass.getDeclaredFields())
                .map(field -> {
                    try {
                        return field.get(abstractAuthorityClass).toString();
                    } catch (IllegalAccessException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var serverAuthorities = Stream.of(authorityClass.getEnumConstants())
                .map(AbstractAuthority::getValue)
                .collect(Collectors.toSet());
        actualAuthorities.addAll(jbstAuthorities);
        actualAuthorities.addAll(serverAuthorities);
        assertTrueOrThrow(
                expectedAuthorities.equals(actualAuthorities),
                "Please verify AbstractAuthority sub enum configuration. Expected: `" + expectedAuthorities + "`. Actual: `" + actualAuthorities + "`"
        );
    }

    // =================================================================================================================
    // Private Method: Reflection on AbstractAuthority
    // =================================================================================================================
    @SuppressWarnings("unchecked")
    private Set<Class<? extends AbstractAuthority>> getAbstractAuthorityClasses(String packageName) {
        var beanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
        var classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(beanDefinitionRegistry, false);
        var tf = new AssignableTypeFilter(AbstractAuthority.class);
        classPathBeanDefinitionScanner.addIncludeFilter(tf);
        classPathBeanDefinitionScanner.scan(packageName);
        return Stream.of(beanDefinitionRegistry.getBeanDefinitionNames())
                .map(beanDefinitionRegistry::getBeanDefinition)
                .map(BeanDefinition::getBeanClassName)
                .map(className -> {
                    try {
                        return (Class<? extends AbstractAuthority>) Class.forName(className);
                    } catch (ClassNotFoundException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(clazz -> nonNull(clazz.getEnumConstants()))
                .collect(Collectors.toSet());
    }
}
