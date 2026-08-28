package com.sangiya.multitenant;

import com.sangiya.multitenant.product.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void create_savesAndReturnsProduct() {
        Product saved = new Product("Widget", "A widget", new BigDecimal("9.99"), 100);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = productService.create("Widget", "A widget", new BigDecimal("9.99"), 100);

        assertThat(result.getName()).isEqualTo("Widget");
        assertThat(result.getPrice()).isEqualByComparingTo("9.99");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void findById_existingProduct_returnsIt() {
        Product product = new Product("Gadget", null, new BigDecimal("49.99"), 10);
        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        Product result = productService.findById("P1");

        assertThat(result.getName()).isEqualTo("Gadget");
    }

    @Test
    void findById_unknownId_throwsNotFoundException() {
        when(productRepository.findById("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById("MISSING"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("MISSING");
    }

    @Test
    void findAll_returnsAllProducts() {
        Product p1 = new Product("A", null, BigDecimal.ONE, 5);
        Product p2 = new Product("B", null, BigDecimal.TEN, 3);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Product> all = productService.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void search_delegatesToRepository() {
        Product p = new Product("Widget Pro", null, new BigDecimal("19.99"), 50);
        when(productRepository.findByNameContainingIgnoreCase("widget")).thenReturn(List.of(p));

        List<Product> results = productService.search("widget");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Widget Pro");
    }

    @Test
    void update_existingProduct_updatesFields() {
        Product product = new Product("Old Name", "Old desc", new BigDecimal("5.00"), 10);
        when(productRepository.findById("P1")).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);

        Product result = productService.update("P1", "New Name", "New desc", new BigDecimal("15.00"), 20);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualByComparingTo("15.00");
        assertThat(result.getStockQuantity()).isEqualTo(20);
    }

    @Test
    void delete_existingProduct_removesIt() {
        Product product = new Product("Temp", null, BigDecimal.ONE, 1);
        when(productRepository.findById("P1")).thenReturn(Optional.of(product));

        productService.delete("P1");

        verify(productRepository).delete(product);
    }

    @Test
    void delete_unknownId_throwsNotFoundException() {
        when(productRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete("GHOST"))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
