package jbst.iam.filters.jwt_extension;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseJwtTokensFilterExtension implements JwtTokensFilterExtension {

    @Override
    public void doFilter(@NotNull HttpServletRequest request) {
        // no actions
    }
}
