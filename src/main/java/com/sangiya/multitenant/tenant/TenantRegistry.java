package com.sangiya.multitenant.tenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TenantRegistry {

    private final Set<String> tenants;

    public TenantRegistry(@Value("${multitenancy.tenants}") String tenantsCsv) {
        this.tenants = Arrays.stream(tenantsCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
    }

    public boolean isValid(String tenantId) {
        return tenants.contains(tenantId);
    }

    public Set<String> getAll() {
        return tenants;
    }
}
