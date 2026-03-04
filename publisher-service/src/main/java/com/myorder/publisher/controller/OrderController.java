package com.myorder.publisher.controller;

import com.myorder.publisher.dto.OrderEvent;
import com.myorder.publisher.service.OrderPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderPublisherService orderPublisherService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalAmount(request.getTotalAmount())
                .status(OrderEvent.OrderStatus.CREATED.name())
                .build();
        
        orderPublisherService.publishOrderCreatedEvent(event);
        
        return ResponseEntity.ok("Order created successfully with ID: " + orderId);
    }

    @PostMapping("/{orderId}/status")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status) {
        
        OrderEvent event = OrderEvent.builder()
                .orderId(orderId)
                .status(status)
                .build();
        
        orderPublisherService.publishOrderEventWithRoutingKey(
            "order.status.updated", 
            event
        );
        
        return ResponseEntity.ok("Order status update published for ID: " + orderId);
    }

    // Request DTO
    @lombok.Data
    public static class OrderRequest {
        private String customerId;
        private String productId;
        private Integer quantity;
        private BigDecimal totalAmount;
    }
}