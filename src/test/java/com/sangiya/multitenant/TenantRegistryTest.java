package com.sangiya.multitenant;

import com.sangiya.multitenant.tenant.TenantRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantRegistryTest {

    private final TenantRegistry registry = new TenantRegistry("alpha,beta,gamma");

    @Test
    void isValid_knownTenant_returnsTrue() {
        assertThat(registry.isValid("alpha")).isTrue();
        assertThat(registry.isValid("beta")).isTrue();
        assertThat(registry.isValid("gamma")).isTrue();
    }

    @Test
    void isValid_unknownTenant_returnsFalse() {
        assertThat(registry.isValid("unknown")).isFalse();
        assertThat(registry.isValid("")).isFalse();
    }

    @Test
    void getAll_returnsAllTenants() {
        assertThat(registry.getAll()).containsExactlyInAnyOrder("alpha", "beta", "gamma");
    }

    @Test
    void isValid_caseNormalization() {
        TenantRegistry reg = new TenantRegistry("ALPHA, BETA");
        assertThat(reg.isValid("alpha")).isTrue();
        assertThat(reg.isValid("beta")).isTrue();
    }
}
