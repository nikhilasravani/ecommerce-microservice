package com.microservices.ecommerce.order.repository;

import com.microservices.ecommerce.order.model.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItems, UUID> {
}
