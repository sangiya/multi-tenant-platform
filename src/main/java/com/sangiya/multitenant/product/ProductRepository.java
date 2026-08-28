package com.sangiya.multitenant.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);
    List<Product> findByStockQuantityGreaterThan(int minStock);
}
