package com.myorder.publisher.service;

import com.myorder.publisher.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPublisherService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.routing.key.order.created}")
    private String orderCreatedRoutingKey;

    public void publishOrderCreatedEvent(OrderEvent orderEvent) {
        try {
            orderEvent.setCreatedAt(LocalDateTime.now());
            orderEvent.setUpdatedAt(LocalDateTime.now());
            
            log.info("Publishing order created event: {}", orderEvent.getOrderId());
            
            rabbitTemplate.convertAndSend(
                orderExchange,
                orderCreatedRoutingKey,
                orderEvent
            );
            
            log.info("Successfully published order event: {}", orderEvent.getOrderId());
        } catch (Exception e) {
            log.error("Failed to publish order event: {}", orderEvent.getOrderId(), e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }

    public void publishOrderEventWithRoutingKey(String routingKey, OrderEvent orderEvent) {
        try {
            orderEvent.setUpdatedAt(LocalDateTime.now());
            
            log.info("Publishing order event with routing key: {}", routingKey);
            
            rabbitTemplate.convertAndSend(
                orderExchange,
                routingKey,
                orderEvent
            );
            
            log.info("Successfully published order event with routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish order event with routing key: {}", routingKey, e);
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
}