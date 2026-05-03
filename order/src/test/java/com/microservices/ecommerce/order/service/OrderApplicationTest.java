package com.microservices.ecommerce.order.service;

import com.microservices.ecommerce.order.OrderApplication;
import com.microservices.ecommerce.order.dto.CartItemDTO;
import com.microservices.ecommerce.order.dto.CartResponseDTO;
import com.microservices.ecommerce.order.dto.OrderResponseDTO;
import com.microservices.ecommerce.order.enums.OrderStatus;
import com.microservices.ecommerce.order.externalClients.CartFeignClient;
import com.microservices.ecommerce.order.externalClients.ProductFeignClient;
import com.microservices.ecommerce.order.model.Order;
import com.microservices.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderApplicationTest {

    @Mock
    private CartFeignClient cartFeignClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductFeignClient productFeignClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImplementation orderServiceImplementation;

    private UUID userId;
    private CartResponseDTO cartResponseDTO;
    private CartItemDTO item1;
    private CartItemDTO item2;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();

        item1 = new CartItemDTO();
        item1.setProductId(UUID.randomUUID());
        item1.setProductName("Laptop");
        item1.setProductPrice(50000.0);
        item1.setQuantity(2);  // 1,00,000

        item2 = new CartItemDTO();
        item2.setProductId(UUID.randomUUID());
        item2.setProductName("Mouse");
        item2.setProductPrice(500.0);
        item2.setQuantity(1);

        cartResponseDTO = new CartResponseDTO();
        cartResponseDTO.setItems(List.of(item1,item2));

        ReflectionTestUtils.setField(orderServiceImplementation, "cartInternalToken", "cart-token");
        ReflectionTestUtils.setField(orderServiceImplementation, "productInternalToken", "product-token");
    }

    @Test
    void placeOrder_ShouldReturnOrderResponse_WhenCartHasItems() throws Exception {
        OrderResponseDTO expectedResponse = new OrderResponseDTO();
        expectedResponse.setTotalPrice(100500.0);

        when(cartFeignClient.getCartByUserId(userId, "cart-token")).thenReturn(cartResponseDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(i-> i.getArgument(0));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(expectedResponse);

        OrderResponseDTO result = orderServiceImplementation.placeOrder(userId);

        assertNotNull(result);
        assertEquals(100500.0, result.getTotalPrice());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartFeignClient, times(1)).clearCart(userId, "cart-token");
    }

    @Test
    void placeOrder_ShouldThrowException_WhenCartItemsAreNull() {
        cartResponseDTO.setItems(null);  // override @BeforeEach setup

        when(cartFeignClient.getCartByUserId(userId, "cart-token")).thenReturn(cartResponseDTO);

        Exception ex = assertThrows(Exception.class, () -> orderServiceImplementation.placeOrder(userId));
        assertEquals("Cart is empty!!", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_ShouldThrowException_WhenCartItemsAreEmpty() {
        cartResponseDTO.setItems(Collections.emptyList());  // override @BeforeEach setup

        when(cartFeignClient.getCartByUserId(userId, "cart-token")).thenReturn(cartResponseDTO);

        Exception ex = assertThrows(Exception.class, () -> orderServiceImplementation.placeOrder(userId));
        assertEquals("Cart is empty!!", ex.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_ShouldCalculateTotalAmountCorrectly() throws Exception {
        // item1: 50000 * 2 = 100000, item2: 500 * 1 = 500 → total = 100500
        when(cartFeignClient.getCartByUserId(userId, "cart-token")).thenReturn(cartResponseDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.setTotalPrice(o.getTotalAmount());
            return dto;
        });

        OrderResponseDTO result = orderServiceImplementation.placeOrder(userId);

        assertEquals(100500.0, result.getTotalPrice());
    }

    @Test
    void placeOrder_ShouldSetOrderStatusAsPending() throws Exception {
        when(cartFeignClient.getCartByUserId(userId, "cart-token")).thenReturn(cartResponseDTO);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDTO.class))).thenReturn(new OrderResponseDTO());

        orderServiceImplementation.placeOrder(userId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(OrderStatus.PENDING, orderCaptor.getValue().getStatus());
    }

    @Test
    void placeOrder_ShouldCallReduceStock_ForEachCartItem() throws Exception {
        when(cartFeignClient.getCartByUserId(userId, "cart-token")).thenReturn(cartResponseDTO);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(), eq(OrderResponseDTO.class))).thenReturn(new OrderResponseDTO());

        orderServiceImplementation.placeOrder(userId);

        // 2 items → reduceStock called twice
        verify(productFeignClient, times(2)).reduceStock(any(), any(), eq("product-token"));
    }

    @Test
    void placeOrder_ShouldClearCart_AfterOrderSaved() throws Exception {
        when(cartFeignClient.getCartByUserId(userId, "cart-token")).thenReturn(cartResponseDTO);
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(modelMapper.map(any(), eq(OrderResponseDTO.class))).thenReturn(new OrderResponseDTO());

        orderServiceImplementation.placeOrder(userId);

        verify(cartFeignClient, times(1)).clearCart(userId, "cart-token");
    }

    @Test
    void placeOrder_ShouldThrowException_WhenCartFeignClientFails() {
        when(cartFeignClient.getCartByUserId(userId, "cart-token"))
                .thenThrow(new RuntimeException("Cart service unavailable"));

        assertThrows(RuntimeException.class, () -> orderServiceImplementation.placeOrder(userId));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrdersByUserId_ShouldReturnOrders_WhenOrdersExist() {
        Order order1 = new Order();
        Order order2 = new Order();

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order1, order2));
        when(modelMapper.map(order1, OrderResponseDTO.class)).thenReturn(new OrderResponseDTO());
        when(modelMapper.map(order2, OrderResponseDTO.class)).thenReturn(new OrderResponseDTO());

        List<OrderResponseDTO> result = orderServiceImplementation.getOrdersByUserId(userId);

        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getOrdersByUserId_ShouldReturnEmptyList_WhenNoOrdersFound() {
        when(orderRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        List<OrderResponseDTO> result = orderServiceImplementation.getOrdersByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrdersByUserId_ShouldMapEachOrderToDTO() {
        Order order = new Order();
        order.setTotalAmount(5000.0);

        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setTotalPrice(5000.0);

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(dto);

        List<OrderResponseDTO> result = orderServiceImplementation.getOrdersByUserId(userId);

        assertEquals(5000.0, result.get(0).getTotalPrice());
        verify(modelMapper, times(1)).map(order, OrderResponseDTO.class);
    }


}
