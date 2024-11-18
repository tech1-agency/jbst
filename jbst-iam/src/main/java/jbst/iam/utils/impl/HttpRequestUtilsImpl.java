package jbst.iam.utils.impl;

import jakarta.servlet.http.HttpServletRequest;
import jbst.iam.utils.HttpRequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jbst.foundation.domain.http.cache.CachedBodyHttpServletRequest;
import jbst.foundation.domain.properties.JbstProperties;

import static jbst.foundation.utilities.http.HttpServletRequestUtility.isPOST;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpRequestUtilsImpl implements HttpRequestUtils {
    protected static final String CACHED_PAYLOAD_ATTRIBUTE = "jbst-security-jwt-cached-payload-attribute";

    // Properties
    private final JbstProperties jbstProperties;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public boolean isCachedEndpoint(HttpServletRequest request) {
        return this.isAuthenticationLoginEndpoint(request);
    }

    @Override
    public void cachePayload(CachedBodyHttpServletRequest cachedRequest) {
        if (this.isCachedEndpoint(cachedRequest)) {
            cachedRequest.setAttribute(CACHED_PAYLOAD_ATTRIBUTE, cachedRequest.getCachedPayload().value());
        }
    }

    @Override
    public String getCachedPayload(HttpServletRequest request) {
        return (String) request.getAttribute(CACHED_PAYLOAD_ATTRIBUTE);
    }

    @Override
    public boolean isAuthenticationLoginEndpoint(HttpServletRequest request) {
        return isPOST(request) && this.isEndpoint(request, "/authentication/login");
    }

    @Override
    public boolean isAuthenticationRefreshTokenEndpoint(HttpServletRequest request) {
        return isPOST(request) && this.isEndpoint(request, "/authentication/refreshToken");
    }

    // =================================================================================================================
    // PRIVATE METHODS
    // =================================================================================================================
    private boolean isEndpoint(HttpServletRequest request, String requestMapping) {
        var frameworkBasePathPrefix = this.jbstProperties.getMvcConfigs().getBasePathPrefix();
        var requestURI = this.contextPath + frameworkBasePathPrefix + requestMapping;
        return requestURI.equals(request.getRequestURI());
    }
}
