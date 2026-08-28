package com.sangiya.multitenant.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product product = productService.create(
                request.name(), request.description(),
                request.price(), request.stockQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(ProductResponse.from(productService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list(
            @RequestParam(value = "search", required = false) String search) {
        List<Product> products = (search != null && !search.isBlank())
                ? productService.search(search)
                : productService.findAll();
        return ResponseEntity.ok(products.stream().map(ProductResponse::from).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable("id") String id,
            @Valid @RequestBody ProductRequest request) {
        Product updated = productService.update(
                id, request.name(), request.description(),
                request.price(), request.stockQuantity());
        return ResponseEntity.ok(ProductResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @Positive BigDecimal price,
            @PositiveOrZero int stockQuantity) {}

    public record ProductResponse(
            String id,
            String name,
            String description,
            BigDecimal price,
            int stockQuantity,
            Instant createdAt,
            Instant updatedAt) {

        static ProductResponse from(Product p) {
            return new ProductResponse(
                    p.getId(), p.getName(), p.getDescription(),
                    p.getPrice(), p.getStockQuantity(),
                    p.getCreatedAt(), p.getUpdatedAt());
        }
    }

    public record ErrorResponse(String error) {}
}
