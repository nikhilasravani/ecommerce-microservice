package com.microservices.ecommerce.order.service;

import com.microservices.ecommerce.order.dto.CartResponseDTO;
import com.microservices.ecommerce.order.dto.OrderResponseDTO;
import com.microservices.ecommerce.order.dto.StockUpdateRequestDTO;
import com.microservices.ecommerce.order.enums.OrderStatus;
import com.microservices.ecommerce.order.externalClients.CartFeignClient;
import com.microservices.ecommerce.order.externalClients.ProductFeignClient;
import com.microservices.ecommerce.order.model.Order;
import com.microservices.ecommerce.order.model.OrderItems;
import com.microservices.ecommerce.order.repository.OrderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImplementation implements OrderService {

    private final CartFeignClient cartFeignClient;
    private final ProductFeignClient productFeignClient;
    private final String cartInternalToken;
    private final String productInternalToken;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public OrderServiceImplementation(CartFeignClient cartFeignClient,
                                      ProductFeignClient productFeignClient,
                                      @Value("${services.cart.internal-token}")String cartInternalToken,
                                      @Value("${services.product.internal-token}")String productInternalToken,
                                      OrderRepository orderRepository,
                                      ModelMapper modelMapper) {
        this.cartFeignClient = cartFeignClient;
        this.productFeignClient = productFeignClient;
        this.cartInternalToken = cartInternalToken;
        this.productInternalToken = productInternalToken;
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderResponseDTO placeOrder(UUID userId) throws Exception{
        //Step 1: Get cart items using feign client
        CartResponseDTO cartResponseDTO = cartFeignClient.getCartByUserId(userId, cartInternalToken);

        //Step 2: check if the cart is empty
        if(cartResponseDTO.getItems() == null || cartResponseDTO.getItems().isEmpty()){
            throw new Exception("Cart is empty!!");
        }

        //Step: 3 Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        //Step: 4 Map cart items to the order items
        List<OrderItems> orderItems = cartResponseDTO.getItems().
                stream().map(cartItem ->{
                    //for each cart item we are creating order item
                    OrderItems orderItem = new OrderItems();
                    orderItem.setProductId(cartItem.getProductId());
                    orderItem.setProductName(cartItem.getProductName());
                    orderItem.setProductPrice(cartItem.getProductPrice());
                    orderItem.setQuantity(cartItem.getQuantity());

                    orderItem.setOrder(order);

                    return orderItem;
                })
                .toList();

        //Step:5 Calculate the total amount
        Double totalAmount = orderItems.stream().
                mapToDouble(item -> item.getProductPrice()*item.getQuantity())
                .sum();

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        //Step:6 Reduce product stock
        orderItems.forEach(orderItem ->
                productFeignClient.reduceStock(
                        orderItem.getProductId(),
                        new StockUpdateRequestDTO(orderItem.getQuantity()),
                        productInternalToken
                )
        );

        //Step:6 Save to Repository
        orderRepository.save(order);

        //Step:7 clear cart
        cartFeignClient.clearCart(userId, cartInternalToken);

        //Step:8 Return response
        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(order->modelMapper.map(order, OrderResponseDTO.class))
                .toList();
    }
}
