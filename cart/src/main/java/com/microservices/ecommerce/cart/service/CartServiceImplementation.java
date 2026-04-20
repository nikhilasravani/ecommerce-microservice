package com.microservices.ecommerce.cart.service;

import com.microservices.ecommerce.cart.dto.CartItemRequestDTO;
import com.microservices.ecommerce.cart.dto.CartResponseDTO;
import com.microservices.ecommerce.cart.dto.ProductResponseDTO;
import com.microservices.ecommerce.cart.dto.UserResponseDTO;
import com.microservices.ecommerce.cart.exception.UserNotFoundException;
import com.microservices.ecommerce.cart.externalClients.ProductFeignClient;
import com.microservices.ecommerce.cart.externalClients.UserFeignClient;
import com.microservices.ecommerce.cart.model.Cart;
import com.microservices.ecommerce.cart.model.CartItem;
import com.microservices.ecommerce.cart.repository.CartItemRepository;
import com.microservices.ecommerce.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartServiceImplementation implements  CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;
    private final String userInternalToken;

    public CartServiceImplementation(CartRepository cartRepository,
                                     CartItemRepository cartItemRepository,
                                     ModelMapper modelMapper,
                                     ProductFeignClient productFeignClient,
                                     UserFeignClient userFeignClient,
                                     @Value("${services.cart.internal-token}") String userInternalToken) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
        this.productFeignClient = productFeignClient;
        this.userFeignClient = userFeignClient;
        this.userInternalToken = userInternalToken;
    }

    @Override
    public CartResponseDTO getCartByUserId(UUID userId) throws UserNotFoundException {
        //Step 1 : Validate user exists via FeignClient
        UserResponseDTO user = userFeignClient.getUserById(userId, userInternalToken);
        if(user == null){
            throw new UserNotFoundException("User not found");
        }
        //Find cart for the userId.
        //If found return it
        //If not found, create a new empty cart, assign user to the cart and save it to DB
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(()-> {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            return cartRepository.save(newCart);
        });
        return modelMapper.map(cart, CartResponseDTO.class);
    }

    @Override
    public CartResponseDTO addItemToCart(UUID userId, CartItemRequestDTO request) throws UserNotFoundException {
        //Step 1 : Validate user exists via FeignClient
        UserResponseDTO user = userFeignClient.getUserById(userId, userInternalToken);
        if(user == null){
            throw new UserNotFoundException("User not found");
        }

        //step 2 : Get or create cart for userId
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(()-> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });

        ProductResponseDTO product = productFeignClient.getProductById(request.getProductId());
        //step 3 : check if product already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item-> item.getProductId().equals(request.getProductId()))
                .findFirst();

        //step 4 : Two cases
        if (existingItem.isPresent()) {
            //product already in the cart, increase the quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        }
        else {
            //new product - create fresh CartItem
            CartItem cartItem = new CartItem();
            cartItem.setProductId(product.getProductId());
            cartItem.setProductName(product.getProductName());
            cartItem.setProductPrice(product.getProductPrice());
            cartItem.setQuantity(request.getQuantity());//quantity comes from user request and not from product
            cartItem.setCart(cart);//CartItem knows "I belong to this cart"

            cart.getItems().add(cartItem);//Cart knows "This new item is in my list"

        }
        cartRepository.save(cart);
        return modelMapper.map(cart, CartResponseDTO.class);
    }

    @Override
    public CartResponseDTO updateQuantityOfCartItem(UUID userId, UUID cartItemId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(()-> new RuntimeException("Cart not found"));
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found!"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to the user's cart");
        }

        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero. Use DELETE to remove the item.");
        }

        item.setQuantity(quantity);
        cartRepository.save(cart);
        return modelMapper.map(cart, CartResponseDTO.class);
    }

    @Override
    public CartResponseDTO removeItemFromCart(UUID userId, UUID cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found!"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Cart item does not belong to the user's cart");
        }

        cart.getItems().remove(item);
        cartRepository.save(cart);
        return modelMapper.map(cart, CartResponseDTO.class);
    }

    @Override
    public CartResponseDTO clearCart(UUID userId) throws UserNotFoundException {
        //Step 1 : Validate user exists via FeignClient
        UserResponseDTO user = userFeignClient.getUserById(userId, userInternalToken);
        if(user == null){
            throw new UserNotFoundException("User not found");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);
        return modelMapper.map(cart, CartResponseDTO.class);
    }
}
