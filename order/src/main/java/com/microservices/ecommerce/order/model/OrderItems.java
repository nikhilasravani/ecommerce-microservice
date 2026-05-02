package com.microservices.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="order_items")
public class OrderItems {

    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID orderItemId;

    private UUID productId;
    private String productName;
    private Double productPrice;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;
}
