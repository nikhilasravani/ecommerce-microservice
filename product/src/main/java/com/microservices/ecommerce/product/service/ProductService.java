package com.microservices.ecommerce.product.service;

import com.microservices.ecommerce.product.dto.ProductRequestDTO;
import com.microservices.ecommerce.product.dto.ProductResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponseDTO addProduct(ProductRequestDTO productRequestDTO);

    List<ProductResponseDTO> findAllProducts();

    ProductResponseDTO findProductById(UUID productId);

    ProductResponseDTO updateProduct(UUID productId, ProductRequestDTO productRequestDTO);

    void deleteProduct(UUID productId);
}
