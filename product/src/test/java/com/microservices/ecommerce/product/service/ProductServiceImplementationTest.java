package com.microservices.ecommerce.product.service;

import com.microservices.ecommerce.product.dto.ProductRequestDTO;
import com.microservices.ecommerce.product.dto.ProductResponseDTO;
import com.microservices.ecommerce.product.exception.InvalidInputException;
import com.microservices.ecommerce.product.exception.ProductAlreadyExistsException;
import com.microservices.ecommerce.product.exception.ProductNotFoundException;
import com.microservices.ecommerce.product.model.Product;
import com.microservices.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplementationTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImplementation productServiceImplementation;

    private ProductRequestDTO productRequestDTO;
    private Product product;
    private Product savedProduct;
    private ProductResponseDTO productResponseDTO;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        productRequestDTO = ProductRequestDTO.builder()
                .productName("Wireless Mouse")
                .productDescription("Ergonomic wireless mouse")
                .productPrice(799.0)
                .productStock(25)
                .build();

        product = Product.builder()
                .productName("Wireless Mouse")
                .productDescription("Ergonomic wireless mouse")
                .productPrice(799.0)
                .productStock(25)
                .build();

        savedProduct = Product.builder()
                .productId(productId)
                .productName("Wireless Mouse")
                .productDescription("Ergonomic wireless mouse")
                .productPrice(799.0)
                .productStock(25)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productResponseDTO = ProductResponseDTO.builder()
                .productId(productId)
                .productName("Wireless Mouse")
                .productDescription("Ergonomic wireless mouse")
                .productPrice(799.0)
                .productStock(25)
                .createdAt(savedProduct.getCreatedAt())
                .updatedAt(savedProduct.getUpdatedAt())
                .build();
    }

    @Test
    void addProductSuccessTest() {
        when(productRepository.existsByProductName(productRequestDTO.getProductName())).thenReturn(false);
        when(modelMapper.map(productRequestDTO, Product.class)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductResponseDTO.class)).thenReturn(productResponseDTO);

        ProductResponseDTO result = productServiceImplementation.addProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals("Wireless Mouse", result.getProductName());
        verify(productRepository, times(1)).existsByProductName(productRequestDTO.getProductName());
        verify(modelMapper, times(1)).map(productRequestDTO, Product.class);
        verify(productRepository, times(1)).save(product);
        verify(modelMapper, times(1)).map(savedProduct, ProductResponseDTO.class);
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void addProductDuplicateNameTest() {
        when(productRepository.existsByProductName(productRequestDTO.getProductName())).thenReturn(true);

        ProductAlreadyExistsException exception = assertThrows(
                ProductAlreadyExistsException.class,
                () -> productServiceImplementation.addProduct(productRequestDTO)
        );

        assertEquals("Product name already exists", exception.getMessage());
        verify(productRepository, times(1)).existsByProductName(productRequestDTO.getProductName());
        verify(productRepository, never()).save(any());
        verify(modelMapper, never()).map(any(), eq(Product.class));
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void addProductInvalidPriceTest() {
        productRequestDTO.setProductPrice(0.0);

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productServiceImplementation.addProduct(productRequestDTO)
        );

        assertEquals("Price must be greater than zero", exception.getMessage());
        verify(productRepository, never()).existsByProductName(any());
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void addProductInvalidStockTest() {
        productRequestDTO.setProductStock(-1);

        InvalidInputException exception = assertThrows(
                InvalidInputException.class,
                () -> productServiceImplementation.addProduct(productRequestDTO)
        );

        assertEquals("Stock cannot be negative", exception.getMessage());
        verify(productRepository, never()).existsByProductName(any());
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void findAllProductsTest() {
        when(productRepository.findAll()).thenReturn(List.of(savedProduct));
        when(modelMapper.map(savedProduct, ProductResponseDTO.class)).thenReturn(productResponseDTO);

        List<ProductResponseDTO> result = productServiceImplementation.findAllProducts();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(productId, result.getFirst().getProductId());
        verify(productRepository, times(1)).findAll();
        verify(modelMapper, times(1)).map(savedProduct, ProductResponseDTO.class);
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void findProductByIdSuccessTest() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(savedProduct));
        when(modelMapper.map(savedProduct, ProductResponseDTO.class)).thenReturn(productResponseDTO);

        ProductResponseDTO result = productServiceImplementation.findProductById(productId);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        verify(productRepository, times(1)).findById(productId);
        verify(modelMapper, times(1)).map(savedProduct, ProductResponseDTO.class);
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void findProductByIdNotFoundTest() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productServiceImplementation.findProductById(productId)
        );

        assertEquals("Product not found with ID : " + productId, exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void updateProductSuccessTest() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(savedProduct));
        when(productRepository.save(savedProduct)).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductResponseDTO.class)).thenReturn(productResponseDTO);

        ProductResponseDTO result = productServiceImplementation.updateProduct(productId, productRequestDTO);

        assertNotNull(result);
        assertEquals("Wireless Mouse", result.getProductName());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).existsByProductName(any());
        verify(productRepository, times(1)).save(savedProduct);
        verify(modelMapper, times(1)).map(savedProduct, ProductResponseDTO.class);
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void updateProductDuplicateNameTest() {
        Product existingProduct = Product.builder()
                .productId(productId)
                .productName("Old Name")
                .productDescription("Old description")
                .productPrice(500.0)
                .productStock(10)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByProductName(productRequestDTO.getProductName())).thenReturn(true);

        ProductAlreadyExistsException exception = assertThrows(
                ProductAlreadyExistsException.class,
                () -> productServiceImplementation.updateProduct(productId, productRequestDTO)
        );

        assertEquals("Product name already exists", exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).existsByProductName(productRequestDTO.getProductName());
        verify(productRepository, never()).save(any());
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void updateProductNotFoundTest() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productServiceImplementation.updateProduct(productId, productRequestDTO)
        );

        assertEquals("Product not found with ID : " + productId, exception.getMessage());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any());
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void deleteProductSuccessTest() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(savedProduct));

        assertDoesNotThrow(() -> productServiceImplementation.deleteProduct(productId));

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).delete(savedProduct);
        verifyNoMoreInteractions(productRepository, modelMapper);
    }

    @Test
    void deleteProductNotFoundTest() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productServiceImplementation.deleteProduct(1L)
        );

        assertEquals("Product not found with ID : 1", exception.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).delete(any());
        verifyNoMoreInteractions(productRepository, modelMapper);
    }
}
