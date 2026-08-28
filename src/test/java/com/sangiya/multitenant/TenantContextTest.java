package com.sangiya.multitenant;

import com.sangiya.multitenant.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContextTest {

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void set_andGet_returnsSameTenant() {
        TenantContext.set("alpha");
        assertThat(TenantContext.get()).isEqualTo("alpha");
    }

    @Test
    void clear_removesCurrentTenant() {
        TenantContext.set("beta");
        TenantContext.clear();
        assertThat(TenantContext.get()).isNull();
    }

    @Test
    void get_withoutSetting_returnsNull() {
        assertThat(TenantContext.get()).isNull();
    }

    @Test
    void set_overridesPreviousValue() {
        TenantContext.set("alpha");
        TenantContext.set("gamma");
        assertThat(TenantContext.get()).isEqualTo("gamma");
    }
}
