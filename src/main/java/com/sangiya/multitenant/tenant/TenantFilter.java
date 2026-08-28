package com.sangiya.multitenant.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class TenantFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);
    static final String TENANT_HEADER = "X-Tenant-ID";

    private final TenantRegistry tenantRegistry;

    public TenantFilter(TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip non-API paths (actuator, h2-console)
        String path = httpRequest.getRequestURI();
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String tenantId = httpRequest.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Missing required header: X-Tenant-ID\"}");
            return;
        }

        tenantId = tenantId.trim().toLowerCase();

        if (!tenantRegistry.isValid(tenantId)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Unknown tenant: " + tenantId + "\"}");
            return;
        }

        TenantContext.set(tenantId);
        log.debug("Tenant context set: {}", tenantId);

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
