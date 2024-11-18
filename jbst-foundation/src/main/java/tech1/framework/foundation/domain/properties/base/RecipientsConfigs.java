package tech1.framework.foundation.domain.properties.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import tech1.framework.foundation.domain.constants.JbsConstants;
import tech1.framework.foundation.domain.properties.annotations.MandatoryProperty;

import java.util.List;

// Lombok (property-based)
@AllArgsConstructor(onConstructor = @__({@ConstructorBinding}))
@Data
@EqualsAndHashCode(callSuper = true)
public class RecipientsConfigs extends AbstractPropertyConfigs {
    @MandatoryProperty
    private final List<String> to;

    public static RecipientsConfigs testsHardcoded() {
        return new RecipientsConfigs(
                List.of(
                        "test1@" + JbsConstants.Domains.HARDCODED,
                        "test2@" + JbsConstants.Domains.HARDCODED
                )
        );
    }
}