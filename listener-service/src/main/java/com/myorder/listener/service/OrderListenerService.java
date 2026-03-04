package com.myorder.listener.service;

import com.myorder.listener.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderListenerService {

    @RabbitListener(queues = "${rabbitmq.queue.order.created}", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderCreatedEvent(@Payload OrderEvent orderEvent) {
        try {
            log.info("Received Order Created Event: {}", orderEvent.getOrderId());
            
            // Process the order
            processNewOrder(orderEvent);
            
            log.info("Successfully processed order: {}", orderEvent.getOrderId());
        } catch (Exception e) {
            log.error("Error processing order created event: {}", orderEvent.getOrderId(), e);
            throw new AmqpRejectAndDontRequeueException("Failed to process order", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.order.status}", containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderStatusUpdateEvent(@Payload OrderEvent orderEvent) {
        try {
            log.info("Received Order Status Update Event: {} - Status: {}", 
                orderEvent.getOrderId(), orderEvent.getStatus());
            
            // Process status update
            processStatusUpdate(orderEvent);
            
            log.info("Successfully processed status update for order: {}", orderEvent.getOrderId());
        } catch (Exception e) {
            log.error("Error processing order status update: {}", orderEvent.getOrderId(), e);
            throw new AmqpRejectAndDontRequeueException("Failed to process status update", e);
        }
    }

    private void processNewOrder(OrderEvent orderEvent) {
        // Simulate order processing logic
        log.info("Processing new order for customer: {}", orderEvent.getCustomerId());
        log.info("Product ID: {}, Quantity: {}, Total: {}", 
            orderEvent.getProductId(), 
            orderEvent.getQuantity(), 
            orderEvent.getTotalAmount());
        
        // Add your business logic here
        // - Validate inventory
        // - Process payment
        // - Update database
        // - Send notifications
        
        try {
            // Simulate processing time
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processStatusUpdate(OrderEvent orderEvent) {
        log.info("Updating order status in database: {} -> {}", 
            orderEvent.getOrderId(), 
            orderEvent.getStatus());
        
        // Add your status update logic here
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}