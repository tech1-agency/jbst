package jbst.iam.configurations;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

public interface AbstractJbstSecurityJwtConfigurer {
    void configure(WebSecurity web);
    void configure(HttpSecurity http) throws Exception;
}
