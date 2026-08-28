package com.sangiya.multitenant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${multitenancy.tenants}")
    private String tenantsCsv;

    @Value("${multitenancy.default-tenant}")
    private String defaultTenant;

    @Bean
    @Primary
    public DataSource dataSource() {
        Map<Object, Object> targets = new LinkedHashMap<>();
        DataSource defaultDataSource = null;

        for (String raw : tenantsCsv.split(",")) {
            String tenantId = raw.trim().toLowerCase();
            DataSource ds = buildTenantDataSource(tenantId);
            targets.put(tenantId, ds);
            if (tenantId.equals(defaultTenant.trim().toLowerCase())) {
                defaultDataSource = ds;
            }
        }

        if (defaultDataSource == null) {
            defaultDataSource = (DataSource) targets.values().iterator().next();
        }

        TenantRoutingDataSource routing = new TenantRoutingDataSource();
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(defaultDataSource);

        return routing;
    }

    private DataSource buildTenantDataSource(String tenantId) {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("db_" + tenantId)
                .addScript("classpath:db/schema.sql")
                .build();
    }
}
