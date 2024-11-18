package jbst.foundation.domain.properties.configs;

import jbst.foundation.domain.base.Version;
import jbst.foundation.domain.maven.MavenDetails;
import jbst.foundation.domain.properties.annotations.MandatoryProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class MavenConfigs extends AbstractPropertiesConfigs {
    @MandatoryProperty
    private final String groupId;
    @MandatoryProperty
    private final String artifactId;
    @MandatoryProperty
    private final Version version;

    public static MavenConfigs testsHardcoded() {
        return new MavenConfigs("tech1.framework", "tech1-framework", Version.testsHardcoded());
    }

    @Override
    public boolean isParentPropertiesNode() {
        return true;
    }

    public MavenDetails asMavenDetails() {
        return new MavenDetails(this.groupId, this.artifactId, this.version);
    }
}
