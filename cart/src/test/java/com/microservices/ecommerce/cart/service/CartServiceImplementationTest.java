package com.microservices.ecommerce.cart.service;

import com.microservices.ecommerce.cart.dto.*;
import com.microservices.ecommerce.cart.exception.CartItemNotFoundException;
import com.microservices.ecommerce.cart.exception.CartNotFoundException;
import com.microservices.ecommerce.cart.exception.UserNotFoundException;
import com.microservices.ecommerce.cart.externalClients.ProductFeignClient;
import com.microservices.ecommerce.cart.externalClients.UserFeignClient;
import com.microservices.ecommerce.cart.model.Cart;
import com.microservices.ecommerce.cart.model.CartItem;
import com.microservices.ecommerce.cart.repository.CartItemRepository;
import com.microservices.ecommerce.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
public class CartServiceImplementationTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ProductFeignClient productFeignClient;
    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private CartServiceImplementation cartService;

    private UUID userId;
    private UserResponseDTO userResponseDTO;
    private Cart cart;
    private CartResponseDTO cartResponseDTO;
    private UUID cartItemId;


    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(cartService, "userInternalToken", "test-token");

        //Reusable UUID
        userId = UUID.randomUUID();

        //Create fake responseDTO that the mocked FeignClient will return
        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUserId(userId);

        // Create an empty Cart object that the mocked repo can return
        cart= new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());

        // Create what ModelMapper should return when mapping a Cart
        cartResponseDTO = new CartResponseDTO();
        cartResponseDTO.setUserId(userId);

        cartItemId = UUID.randomUUID();
    }

    @Test
    void getCartByUserId() throws UserNotFoundException {
        //Arrange : tell mock what to return
        when(userFeignClient.getUserById(userId, "test-token")).thenReturn(userResponseDTO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(modelMapper.map(cart, CartResponseDTO.class)).thenReturn(cartResponseDTO);

        //Act : call the real method
        CartResponseDTO result = cartService.getCartByUserId(userId);

        //Assert : verify the result with what you expect
        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        //verify the repo was called exactly once
        verify(cartRepository,times(1)).findByUserId(userId);
        //verify SAVE was never called (cart already existed, no need to create)
        verify(cartRepository,never()).save(cart);
    }

    @Test
    void getCartByUserIdWhenCartNotFound() throws UserNotFoundException {
        when(userFeignClient.getUserById(userId, "test-token")).thenReturn(userResponseDTO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(modelMapper.map(cart, CartResponseDTO.class)).thenReturn(cartResponseDTO);

        CartResponseDTO result = cartService.getCartByUserId(userId);
        assertNotNull(result);

        verify(cartRepository,times(1)).save(any(Cart.class));
    }

    @Test
    void getCartByUserIdWhenUserNotFound() throws UserNotFoundException {
        when(userFeignClient.getUserById(userId,"test-token")).thenReturn(null);

        assertThrows(UserNotFoundException.class, ()-> cartService.getCartByUserId(userId));

        verify(cartRepository,never()).findByUserId(any());

    }

    @Test
    void addItemToCart_WhenUserNotFound_ShouldThrowException() throws UserNotFoundException {

        //Arrange
        when(userFeignClient.getUserById(userId,"test-token")).thenReturn(null);

        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(4);

        //Act + Assert
        assertThrows(UserNotFoundException.class, () -> cartService.addItemToCart(userId, request));

        verify(cartRepository,never()).findByUserId(any());
    }

    @Test
    void addItemToCart_WhenUserFound_AddProduct() throws UserNotFoundException {

        //Arrange
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(4);

        ProductResponseDTO product = new ProductResponseDTO();
        product.setProductId(request.getProductId());
        product.setProductName("TestProduct");
        product.setProductPrice(100.0);

        when(userFeignClient.getUserById(userId,"test-token")).thenReturn(userResponseDTO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(productFeignClient.getProductById(product.getProductId())).thenReturn(product);
        when(cartRepository.save(cart)).thenReturn(cart);
        when(modelMapper.map(cart, CartResponseDTO.class)).thenReturn(cartResponseDTO);

        //Act
        CartResponseDTO result = cartService.addItemToCart(userId, request);

        //Assert
        assertNotNull(result);
        assertEquals(1, cart.getItems().size());
        verify(cartRepository,times(1)).save(cart);
    }

    @Test
    void addItemToCart_WhenProductAlreadyInCart_ShouldIncreaseQuantity() throws UserNotFoundException {
        //Arrange
        CartItemRequestDTO request = new CartItemRequestDTO();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(2);

        CartItem existingItem = new CartItem();
        existingItem.setProductId(request.getProductId());
        existingItem.setQuantity(3);
        existingItem.setCart(cart);

        cart.getItems().add(existingItem);

        when(userFeignClient.getUserById(userId,"test-token")).thenReturn(userResponseDTO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(modelMapper.map(cart, CartResponseDTO.class)).thenReturn(cartResponseDTO);

        //verify
        CartResponseDTO result = cartService.addItemToCart(userId, request);
        assertNotNull(result);
        assertEquals(1, cart.getItems().size());
        assertEquals(5, existingItem.getQuantity());
        verify(cartRepository,times(1)).save(cart);
    }

    @Test
    void updateQuantity_WhenCartNotFound_ShouldThrowException()  {

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class,
                ()-> cartService.updateQuantityOfCartItem(userId,cartItemId, 5));
        verify(cartRepository,never()).findById(any());
    }

    @Test
    void updateQuantity_WhenCartItemNotFound_ShouldThrowException(){

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateQuantityOfCartItem(userId,cartItemId, 5));

        verify(cartRepository,never()).save(any());
    }

    @Test
    void updateQuantity_WhenItemDoesNotBelongToCart_ShouldThrowException() {

        CartItem item =  new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(5);

        Cart anotherCart = new Cart();
        anotherCart.setId(UUID.randomUUID());
        item.setCart(anotherCart);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));
        assertThrows(IllegalArgumentException.class,
                () -> cartService.updateQuantityOfCartItem(userId,cartItemId, 5));
        verify(cartRepository,never()).save(any());

    }

    @Test
    void updateQuantity_WhenValid_ShouldUpdateQuantity(){
        CartItem item =  new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(5);
        item.setCart(cart);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));
        when(modelMapper.map(cart, CartResponseDTO.class)).thenReturn(cartResponseDTO);

        cartService.updateQuantityOfCartItem(userId,cartItemId, 5);

        assertEquals(5, item.getQuantity());

        verify(cartRepository,times(1)).save(cart);
    }

    @Test
    void updateQuantity_WhenQuantityIsZeroOrNegative_ShouldThrowException(){
        CartItem item =  new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(3);
        item.setCart(cart);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));
        assertThrows(ResponseStatusException.class,
                () -> cartService.updateQuantityOfCartItem(userId,cartItemId, 0));
        verify(cartRepository,never()).save(any());
    }

    @Test
    void removeItemFromCart_WhenCartNotFound_ShouldThrowException() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class,
                () -> cartService.removeItemFromCart(userId,cartItemId));
        verify(cartRepository,never()).save(cart);
    }

    @Test
    void removeItemFromCart_WhenCartItemNotFound_ShouldThrowException(){
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());
        assertThrows(CartItemNotFoundException.class,
                () -> cartService.removeItemFromCart(userId,cartItemId));
        verify(cartRepository,never()).save(cart);
    }

    @Test
    void removeItemFromCart_WhenItemDoesNotBelongToCart_ShouldThrowException(){
        CartItem item =  new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(5);

        Cart anotherCart = new Cart();
        anotherCart.setId(UUID.randomUUID());
        item.setCart(anotherCart);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.removeItemFromCart(userId,cartItemId));

        verify(cartRepository,never()).save(cart);
    }

    @Test
    void removeItemFromCart_WhenValid_ShouldRemoveItem(){

        CartItem item =  new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(5);
        item.setCart(cart);
        cart.getItems().add(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));
        when(modelMapper.map(cart, CartResponseDTO.class)).thenReturn(cartResponseDTO);

        cartService.removeItemFromCart(userId,cartItemId);

        assertEquals(0, cart.getItems().size());
        verify(cartRepository,times(1)).save(cart);
    }

    @Test
    void clearCart_WhenUserNotFound_ShouldThrowException(){
        when(userFeignClient.getUserById(userId,"test-token")).thenReturn(null);

        assertThrows(UserNotFoundException.class,
                () -> cartService.clearCart(userId));
        verify(cartRepository,never()).save(cart);
    }

    @Test
    void clearCart_WhenCartNotFound_ShouldThrowException(){
        when(userFeignClient.getUserById(userId,"test-token")).thenReturn(userResponseDTO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(CartNotFoundException.class,
                () -> cartService.clearCart(userId));
        verify(cartRepository,never()).save(cart);
    }

    @Test
    void clearCart_WhenValid_ShouldClearAllItems() throws UserNotFoundException {
        CartItem item = new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setQuantity(2);
        item.setCart(cart);
        cart.getItems().add(item);

        when(userFeignClient.getUserById(userId,"test-token")).thenReturn(userResponseDTO);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.clearCart(userId);

        assertEquals(0, cart.getItems().size());
        verify(cartRepository,times(1)).save(cart);

    }
}
