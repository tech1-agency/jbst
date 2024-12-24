package jbst.iam.configurations;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import jbst.foundation.configurations.TestConfigurationPropertiesJbstHardcoded;

@WebAppConfiguration
@ExtendWith({ SpringExtension.class })
@ContextConfiguration(classes = {
        ConfigurationBaseSecurityJwtWebMVC.class,
        TestConfigurationPropertiesJbstHardcoded.class,
        TestConfigurationResources.class
})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public abstract class TestRunnerResources1 extends AbstractTestRunnerResources {

}
