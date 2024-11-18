package jbst.iam.server.base.properties;

import jbst.iam.server.base.properties.server.ServerConfigs;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

@ConfigurationProperties(
        prefix = "tech1-server",
        ignoreUnknownFields = false
)
@Data
public class ApplicationProperties implements PriorityOrdered {
    private ServerConfigs serverConfigs;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
