package com.microservices.ecommerce.product.repository;

import com.microservices.ecommerce.product.model.Product;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsByProductName(@NotNull @Size(max = 100) String productName);
}
