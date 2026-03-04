package com.myorder.listener.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.queue.order.created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queue.order.status}")
    private String orderStatusQueue;

    @Value("${rabbitmq.routing.key.order.created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.routing.key.order.status}")
    private String orderStatusRoutingKey;

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.port}")
    private int port;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.virtual-host}")
    private String virtualHost;

    @Value("${rabbitmq.ssl.enabled}")
    private boolean sslEnabled;

    @Value("${rabbitmq.listener.concurrency:3}")
    private int concurrentConsumers;

    @Value("${rabbitmq.listener.max-concurrency:10}")
    private int maxConcurrentConsumers;

    // Exchange
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange(orderExchange)
                .durable(true)
                .build();
    }

    // Queues
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(orderCreatedQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", orderCreatedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(orderStatusQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", orderStatusQueue + ".dlq")
                .build();
    }

    // Dead Letter Queues
    @Bean
    public Queue orderCreatedDLQ() {
        return QueueBuilder.durable(orderCreatedQueue + ".dlq").build();
    }

    @Bean
    public Queue orderStatusDLQ() {
        return QueueBuilder.durable(orderStatusQueue + ".dlq").build();
    }

    // Bindings
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(orderExchange())
                .with(orderCreatedRoutingKey);
    }

    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder
                .bind(orderStatusQueue())
                .to(orderExchange())
                .with(orderStatusRoutingKey);
    }

    // Connection Factory
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        
        if (sslEnabled) {
            connectionFactory.getRabbitConnectionFactory().useSslProtocol();
        }
        
        connectionFactory.setChannelCacheSize(25);
        connectionFactory.setConnectionTimeout(30000);
        
        return connectionFactory;
    }

    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Rabbit Template (for sending if needed)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // Listener Container Factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}