package com.sangiya.multitenant;

import com.sangiya.multitenant.product.ProductController.ProductRequest;
import com.sangiya.multitenant.product.ProductController.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MultiTenantIsolationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void productCreatedForAlpha_notVisibleToBeta() {
        // Create a product as tenant alpha
        HttpHeaders alphaHeaders = tenantHeaders("alpha");
        ProductRequest request = new ProductRequest(
                "Alpha-Only Widget", "Exclusive to alpha", new BigDecimal("99.99"), 50);

        ResponseEntity<ProductResponse> createResponse = restTemplate.exchange(
                "/api/v1/products", HttpMethod.POST,
                new HttpEntity<>(request, alphaHeaders),
                ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String productId = createResponse.getBody().id();
        assertThat(productId).isNotNull();

        // Alpha can retrieve the product
        ResponseEntity<ProductResponse> alphaGet = restTemplate.exchange(
                "/api/v1/products/" + productId, HttpMethod.GET,
                new HttpEntity<>(alphaHeaders),
                ProductResponse.class);
        assertThat(alphaGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(alphaGet.getBody().name()).isEqualTo("Alpha-Only Widget");

        // Beta cannot see the same product ID
        HttpHeaders betaHeaders = tenantHeaders("beta");
        ResponseEntity<String> betaGet = restTemplate.exchange(
                "/api/v1/products/" + productId, HttpMethod.GET,
                new HttpEntity<>(betaHeaders),
                String.class);
        assertThat(betaGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void eachTenantMaintainsItsOwnCatalog() {
        HttpHeaders alphaHeaders = tenantHeaders("alpha");
        HttpHeaders gammaHeaders = tenantHeaders("gamma");

        // Create separate products for alpha and gamma
        restTemplate.exchange("/api/v1/products", HttpMethod.POST,
                new HttpEntity<>(new ProductRequest("Alpha Product", null, new BigDecimal("10.00"), 10), alphaHeaders),
                ProductResponse.class);

        restTemplate.exchange("/api/v1/products", HttpMethod.POST,
                new HttpEntity<>(new ProductRequest("Gamma Product", null, new BigDecimal("20.00"), 5), gammaHeaders),
                ProductResponse.class);

        // List alpha products: contains alpha's product
        ResponseEntity<ProductResponse[]> alphaList = restTemplate.exchange(
                "/api/v1/products", HttpMethod.GET,
                new HttpEntity<>(alphaHeaders),
                ProductResponse[].class);
        assertThat(alphaList.getStatusCode()).isEqualTo(HttpStatus.OK);
        boolean alphaHasAlphaProduct = java.util.Arrays.stream(alphaList.getBody())
                .anyMatch(p -> "Alpha Product".equals(p.name()));
        assertThat(alphaHasAlphaProduct).isTrue();

        // List gamma products: does NOT contain alpha's product
        ResponseEntity<ProductResponse[]> gammaList = restTemplate.exchange(
                "/api/v1/products", HttpMethod.GET,
                new HttpEntity<>(gammaHeaders),
                ProductResponse[].class);
        boolean gammaHasAlphaProduct = java.util.Arrays.stream(gammaList.getBody())
                .anyMatch(p -> "Alpha Product".equals(p.name()));
        assertThat(gammaHasAlphaProduct).isFalse();
    }

    @Test
    void missingTenantHeader_returns400() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products", HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unknownTenant_returns403() {
        HttpHeaders headers = tenantHeaders("shadow-corp");
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products", HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private HttpHeaders tenantHeaders(String tenantId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", tenantId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
