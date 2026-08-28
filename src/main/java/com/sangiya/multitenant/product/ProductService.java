package com.sangiya.multitenant.product;

import com.sangiya.multitenant.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(String name, String description, BigDecimal price, int stockQuantity) {
        Product product = new Product(name, description, price, stockQuantity);
        Product saved = productRepository.save(product);
        log.info("Product created: id={} tenant={}", saved.getId(), TenantContext.get());
        return saved;
    }

    @Transactional(readOnly = true)
    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public Product update(String id, String name, String description, BigDecimal price, int stockQuantity) {
        Product product = findById(id);
        product.update(name, description, price, stockQuantity);
        Product saved = productRepository.save(product);
        log.info("Product updated: id={} tenant={}", id, TenantContext.get());
        return saved;
    }

    public void delete(String id) {
        Product product = findById(id);
        productRepository.delete(product);
        log.info("Product deleted: id={} tenant={}", id, TenantContext.get());
    }
}
