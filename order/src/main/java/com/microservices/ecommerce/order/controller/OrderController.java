package com.microservices.ecommerce.order.controller;

import com.microservices.ecommerce.order.dto.OrderResponseDTO;
import com.microservices.ecommerce.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<OrderResponseDTO> placeOrder(@PathVariable UUID userId) throws Exception
    {
        OrderResponseDTO orderResponseDTO = orderService.placeOrder(userId);
        return new ResponseEntity<>(orderResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrders(@PathVariable UUID userId) throws Exception
    {
        List<OrderResponseDTO> orderResponseDTO = orderService.getOrdersByUserId(userId);
        return new ResponseEntity<>(orderResponseDTO, HttpStatus.OK);
    }
}
