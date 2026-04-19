package com.microservices.ecommerce.product.service;

import com.microservices.ecommerce.product.dto.ProductRequestDTO;
import com.microservices.ecommerce.product.dto.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO addProduct(ProductRequestDTO productRequestDTO);

    List<ProductResponseDTO> findAllProducts();

    ProductResponseDTO findProductById(Long productId);

    ProductResponseDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO);

    void deleteProduct(Long productId);
}
