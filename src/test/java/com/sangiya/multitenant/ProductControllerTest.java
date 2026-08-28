package com.sangiya.multitenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangiya.multitenant.product.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest {

    private final ProductService productService = mock(ProductService.class);
    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new ProductController(productService))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void createProduct_validRequest_returns201() throws Exception {
        Product product = new Product("Widget", "A widget", new BigDecimal("9.99"), 100);
        when(productService.create(anyString(), anyString(), any(), anyInt())).thenReturn(product);

        String body = """
                {"name":"Widget","description":"A widget","price":9.99,"stockQuantity":100}
                """;

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.price").value(9.99));
    }

    @Test
    void createProduct_missingName_returns400() throws Exception {
        String body = """
                {"description":"A widget","price":9.99,"stockQuantity":100}
                """;

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_negativePrice_returns400() throws Exception {
        String body = """
                {"name":"Widget","price":-1.00,"stockQuantity":10}
                """;

        mvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProduct_existingProduct_returns200() throws Exception {
        Product product = new Product("Gadget", null, new BigDecimal("49.99"), 10);
        when(productService.findById("P1")).thenReturn(product);

        mvc.perform(get("/api/v1/products/P1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gadget"));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        when(productService.findById("MISSING")).thenThrow(new ProductNotFoundException("MISSING"));

        mvc.perform(get("/api/v1/products/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void listProducts_noSearch_returnsAll() throws Exception {
        when(productService.findAll()).thenReturn(List.of(
                new Product("A", null, BigDecimal.ONE, 1),
                new Product("B", null, BigDecimal.TEN, 2)));

        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listProducts_withSearch_callsSearch() throws Exception {
        when(productService.search("wid")).thenReturn(List.of(
                new Product("Widget", null, BigDecimal.ONE, 5)));

        mvc.perform(get("/api/v1/products").param("search", "wid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Widget"));
    }

    @Test
    void deleteProduct_existingProduct_returns204() throws Exception {
        doNothing().when(productService).delete("P1");

        mvc.perform(delete("/api/v1/products/P1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound_returns404() throws Exception {
        doThrow(new ProductNotFoundException("GHOST")).when(productService).delete("GHOST");

        mvc.perform(delete("/api/v1/products/GHOST"))
                .andExpect(status().isNotFound());
    }
}
