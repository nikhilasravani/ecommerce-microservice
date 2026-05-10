package com.microservices.ecommerce.product.service;

import com.microservices.ecommerce.product.dto.ProductRequestDTO;
import com.microservices.ecommerce.product.dto.ProductResponseDTO;
import com.microservices.ecommerce.product.exception.InvalidInputException;
import com.microservices.ecommerce.product.exception.ProductAlreadyExistsException;
import com.microservices.ecommerce.product.exception.ProductNotFoundException;
import com.microservices.ecommerce.product.model.Product;
import com.microservices.ecommerce.product.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImplementation implements ProductService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;

    public ProductServiceImplementation(ModelMapper modelMapper,
                                        ProductRepository productRepository) {
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponseDTO addProduct(ProductRequestDTO productRequestDTO) {
        validateProductRequest(productRequestDTO);
        if (productRepository.existsByProductName(productRequestDTO.getProductName())) {
            throw new ProductAlreadyExistsException("Product name already exists");
        }
        Product product = modelMapper.map(productRequestDTO, Product.class);
        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductResponseDTO.class);
    }

    @Override
    public List<ProductResponseDTO> findAllProducts() {
        return productRepository.findAll().stream()
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .toList();
    }

    @Override
    public ProductResponseDTO findProductById(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID : " + productId));
        return modelMapper.map(product, ProductResponseDTO.class);
    }

    @Override
    public ProductResponseDTO updateProduct(UUID productId, ProductRequestDTO productRequestDTO) {
        validateProductRequest(productRequestDTO);

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID : " + productId));

        if (!existingProduct.getProductName().equals(productRequestDTO.getProductName())
                && productRepository.existsByProductName(productRequestDTO.getProductName())) {
            throw new ProductAlreadyExistsException("Product name already exists");
        }

        existingProduct.setProductName(productRequestDTO.getProductName());
        existingProduct.setProductDescription(productRequestDTO.getProductDescription());
        existingProduct.setProductPrice(productRequestDTO.getProductPrice());
        existingProduct.setProductStock(productRequestDTO.getProductStock());

        Product updatedProduct = productRepository.save(existingProduct);
        return modelMapper.map(updatedProduct, ProductResponseDTO.class);
    }

    @Override
    public void deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID : " + productId));
        productRepository.delete(product);
    }

    private void validateProductRequest(ProductRequestDTO productRequestDTO) {
        if (productRequestDTO.getProductPrice() == null || productRequestDTO.getProductPrice() <= 0) {
            throw new InvalidInputException("Price must be greater than zero");
        }
        if (productRequestDTO.getProductStock() == null || productRequestDTO.getProductStock() < 0) {
            throw new InvalidInputException("Stock cannot be negative");
        }
    }
}
