# MyOrder RabbitMQ EDA - Complete Implementation Guide

A production-ready Event-Driven Architecture implementation using Spring Boot and RabbitMQ, supporting both local development and AWS Amazon MQ deployment.

## Table of Contents
1. Project Overview
2. Architecture
3. Prerequisites
4. Project Structure
5. Configuration Details
6. Local Setup & Execution
7. Testing
8. AWS Deployment
9. Troubleshooting
10. API Reference

---

## Project Overview

This project demonstrates a complete EDA pattern with:
- Publisher Service: Publishes order events to RabbitMQ (Port 8081)
- Listener Service: Consumes and processes order events (Port 8082)
- Message Broker: RabbitMQ (Local Docker / AWS Amazon MQ)
- Exchange Type: Topic Exchange with routing keys
- Message Format: JSON with Jackson serialization

### Key Features
- Environment-specific configurations (Local/Dev/Prod)
- No hardcoded values - all externalized in YAML
- SSL/TLS support for AWS deployment
- Dead Letter Queues (DLQ) for failed messages
- Connection pooling and concurrency management
- Health checks and monitoring endpoints

---

## Architecture
                     CLIENT LAYER
               (Postman, cURL, Web Browser)
                          |
                          v
                PUBLISHER SERVICE (Port 8081)
                REST API -> OrderEvent -> RabbitMQ Template
                          |
                          v
                  MESSAGE BROKER
               RabbitMQ / AWS Amazon MQ
               Exchange (topic) -> Queues
               order.created.queue
               order.status.queue
               *.dlq (Dead Letter)
                          |
                          v
                LISTENER SERVICE (Port 8082)
               @RabbitListener -> OrderEvent -> Business Logic

 
---

## Prerequisites

| Requirement | Version | Download Link |
|-------------|---------|---------------|
| Java JDK | 17+ | https://www.oracle.com/java/technologies/downloads/ or https://adoptium.net/ |
| Apache Maven | 3.8+ | https://maven.apache.org/download.cgi |
| Docker Desktop | Latest | https://www.docker.com/products/docker-desktop |
| Git | Latest | https://git-scm.com/downloads |
| IDE (Optional) | Latest | IntelliJ IDEA or VS Code |

### Verify Installations

java -version
mvn -version
docker --version
docker-compose --version

---

## Project Structure

    myorder-rabbitmq-eda/
    |
    |-- pom.xml                                    (Root Maven POM - Parent)
    |-- docker-compose.yml                         (Local RabbitMQ infrastructure)
    |-- README.md                                  (This documentation file)
    |
    |-- publisher-service/                         (MODULE 1: Event Publisher)
    |   |-- pom.xml                               (Module-specific dependencies)
    |   |-- src/
    |       |-- main/
    |           |-- java/com/myorder/publisher/
    |           |   |-- PublisherServiceApplication.java      (Spring Boot Main)
    |           |   |-- config/
    |           |   |   |-- RabbitMQConfig.java              (Connection & Queue Config)
    |           |   |-- controller/
    |           |   |   |-- OrderController.java             (REST API Endpoints)
    |           |   |-- dto/
    |           |   |   |-- OrderEvent.java                  (Event Data Structure)
    |           |   |-- service/
    |           |       |-- OrderPublisherService.java       (Publishing Logic)
    |           |-- resources/
    |               |-- application.yml                      (Default config)
    |               |-- application-local.yml                (Local profile)
    |               |-- application-dev.yml                  (AWS Dev profile)
    |
    |-- listener-service/                          (MODULE 2: Event Consumer)
        |-- pom.xml                               (Module-specific dependencies)
        |-- src/
            |-- main/
                |-- java/com/myorder/listener/
                |   |-- ListenerServiceApplication.java       (Spring Boot Main)
                |   |-- config/
                |   |   |-- RabbitMQConfig.java              (Listener Configuration)
                |   |-- dto/
                |   |   |-- OrderEvent.java                  (Same event structure)
                |   |-- service/
                |       |-- OrderListenerService.java        (Consumption Logic)
                |-- resources/
                    |-- application.yml                      (Default config)
                    |-- application-local.yml                (Local profile)
                    |-- application-dev.yml                  (AWS Dev profile)

---

## Configuration Details

### Configuration Hierarchy

    application.yml (Default values)
        |
        v
    application-local.yml (Local Docker - overrides for development)
        |
        v
    application-dev.yml (AWS Amazon MQ - production settings)
        |
        v
    Environment Variables (Highest priority - runtime overrides)

### Configuration Files Explained

1. Root pom.xml (Parent POM)
   - Defines Spring Boot version (3.2.0)
   - Manages dependency versions
   - Declares child modules (publisher-service, listener-service)

2. docker-compose.yml (Local Infrastructure)
   Services:
     rabbitmq:
       - Image: rabbitmq:3.12-management-alpine
       - Ports: 5672 (AMQP), 15672 (Management UI)
       - Credentials: admin / admin123
       - Persistent volume for data

3. Publisher Service application.yml
   Key Properties:

     - server.port: 8081
     - rabbitmq.host: ${RABBITMQ_HOST:localhost}
     - rabbitmq.exchange.order: order.exchange
     - rabbitmq.queue.order.created: order.created.queue
     - rabbitmq.routing.key.order.created: order.created

4. Listener Service application.yml
   Key Properties:
     - server.port: 8082
     - rabbitmq.queue.order.created: order.created.queue
     - rabbitmq.queue.order.status: order.status.queue
     - rabbitmq.listener.concurrency: 3 (parallel consumers)

### Profile-Specific Settings

| Setting | Local Profile | Dev Profile (AWS) |
|---------|-------------|-------------------|
| Host | localhost | AWS MQ Endpoint |
| Port | 5672 | 5671 |
| SSL | false | true |
| Username | admin | From env var |
| Password | admin123 | From env var |
| Concurrency | 2-5 | 5-20 |

---

## Local Setup & Execution

### Step 1: Project Setup

Create project directory:
mkdir myorder-rabbitmq-eda
cd myorder-rabbitmq-eda

Create directory structure:
mkdir -p publisher-service/src/main/java/com/myorder/publisher/{config,controller,dto,service}
mkdir -p publisher-service/src/main/resources
mkdir -p listener-service/src/main/java/com/myorder/listener/{config,dto,service}
mkdir -p listener-service/src/main/resources

Create all files (copy from provided source code):
- Root pom.xml
- docker-compose.yml
- All Java files in respective packages
- All YAML files in resources

### Step 2: Start Infrastructure

Start RabbitMQ container:
docker-compose up -d

Verify container is running:
docker ps

View logs (optional):
docker logs myorder-rabbitmq

Access Management UI:
URL: http://localhost:15672
Login: admin / admin123

### Step 3: Build Project

From root directory (myorder-rabbitmq-eda/):
mvn clean install

Expected output:

    [INFO] Building myorder-rabbitmq-eda 1.0.0
    [INFO] --------------------------------[ pom ]---------------------------------
    [INFO] 
    [INFO] --- maven-install-plugin:3.1.1:install (default-install) @ myorder-rabbitmq-eda ---
    [INFO] Installing D:\projects\myorder-rabbitmq-eda\pom.xml to ...
    [INFO] ------------------------------------------------------------------------
    [INFO] Reactor Summary:
    [INFO] 
    [INFO] MyOrder RabbitMQ EDA ............................... SUCCESS [  0.5 s]
    [INFO] Publisher Service .................................. SUCCESS [ 15.2 s]
    [INFO] Listener Service ................................... SUCCESS [ 12.8 s]
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS

### Step 4: Run Services

Option A: Using Maven (Development)

Terminal 1 - Publisher Service:
cd publisher-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

Terminal 2 - Listener Service:
cd listener-service
mvn spring-boot:run -Dspring-boot.run.profiles=local

Option B: Using JAR Files (Production-like)

Build JARs first:

    mvn clean package -DskipTests

Run Publisher:

    java -jar publisher-service/target/publisher-service-1.0.0.jar --spring.profiles.active=local

Run Listener (new terminal):

    java -jar listener-service/target/listener-service-1.0.0.jar --spring.profiles.active=local

Option C: Using IDE (IntelliJ IDEA / Eclipse)

1. Import Project: File -> Open -> Select root pom.xml
2. Wait for Maven sync (auto-download dependencies)
3. Run Publisher: 
   - Navigate to PublisherServiceApplication.java
   - Right-click -> Run
   - Edit Configurations -> Add VM Options: -Dspring.profiles.active=local
4. Run Listener:
   - Navigate to ListenerServiceApplication.java
   - Right-click -> Run
   - Edit Configurations -> Add VM Options: -Dspring.profiles.active=local

### Step 5: Verify Running Services

| Service | Health Check URL | Expected Response |
|---------|-----------------|-------------------|
| Publisher | http://localhost:8081/actuator/health | {"status":"UP"} |
| Listener | http://localhost:8082/actuator/health | {"status":"UP"} |
| RabbitMQ | http://localhost:15672 | Management UI |

---

## Testing

### Test 1: Create Order (Publisher -> Queue)

    cURL Command:
    curl -X POST http://localhost:8081/api/v1/orders \
      -H "Content-Type: application/json" \
      -d '{
        "customerId": "CUST001",
        "productId": "PROD001",
        "quantity": 2,
        "totalAmount": 199.99
      }'
    
    PowerShell Command:
    Invoke-RestMethod -Uri "http://localhost:8081/api/v1/orders" \
      -Method POST \
      -ContentType "application/json" \
      -Body '{
        "customerId": "CUST001",
        "productId": "PROD001",
        "quantity": 2,
        "totalAmount": 199.99
      }'

Expected Response:
Order created successfully with ID: 550e8400-e29b-41d4-a716-446655440000

Verify in RabbitMQ UI:
1. Open http://localhost:15672
2. Login: admin / admin123
3. Navigate to "Queues" tab
4. See message count in order.created.queue

### Test 2: Process Order (Queue -> Listener)

Check Listener Console Output:

    2024-01-15 10:30:45.123  INFO 12345 --- [ntContainer#0-1] c.m.l.s.OrderListenerService             : 
    Received Order Created Event: 550e8400-e29b-41d4-a716-446655440000
    
    2024-01-15 10:30:45.145  INFO 12345 --- [ntContainer#0-1] c.m.l.s.OrderListenerService             : 
    Processing new order for customer: CUST001
    
    2024-01-15 10:30:45.167  INFO 12345 --- [ntContainer#0-1] c.m.l.s.OrderListenerService             : 
    Product ID: PROD001, Quantity: 2, Total: 199.99
    
    2024-01-15 10:30:45.245  INFO 12345 --- [ntContainer#0-1] c.m.l.s.OrderListenerService             : 
    Successfully processed order: 550e8400-e29b-41d4-a716-446655440000

### Test 3: Update Order Status

Command:

    curl -X POST "http://localhost:8081/api/v1/orders/ORDER-123/status?status=PROCESSING"

Expected Response:

Order status update published for ID: ORDER-123

Listener Output:
Received Order Status Update Event: ORDER-123 - Status: PROCESSING
Updating order status in database: ORDER-123 -> PROCESSING
Successfully processed status update for order: ORDER-123

### Test 4: Load Testing

    Send 100 messages:
    for i in {1..100}; do
      curl -X POST http://localhost:8081/api/v1/orders \
        -H "Content-Type: application/json" \
        -d "{
          \"customerId\": \"CUST$i\",
          \"productId\": \"PROD$i\",
          \"quantity\": $i,
          \"totalAmount\": $((i * 10)).99
        }" &
    done

---

## AWS Deployment

### Step 1: Create Amazon MQ Broker

AWS Console Steps:
1. Navigate to Amazon MQ service
2. Click Create broker
3. Select RabbitMQ engine
4. Configure:
   - Broker name: myorder-rabbitmq-broker
   - Deployment mode: Single-instance (dev) or Cluster (prod)
   - Instance type: mq.t3.micro (dev) or mq.m5.large (prod)
   - Username: admin (or your choice)
   - Password: Strong password (save securely)
   - VPC: Your VPC
   - Public accessibility: Yes (for dev/testing)
   - Security group: Allow port 5671 (AMQPS) and 443 (Console)

5. Wait for broker status = Running (10-15 minutes)

### Step 2: Configure Environment Variables

Windows PowerShell:

    $env:RABBITMQ_HOST="b-12345678-1234-1234-1234-123456789012.mq.us-east-1.amazonaws.com"
    $env:RABBITMQ_PORT="5671"
    $env:RABBITMQ_USERNAME="admin"
    $env:RABBITMQ_PASSWORD="YourSecurePassword123!"
    $env:RABBITMQ_SSL_ENABLED="true"
    $env:RABBITMQ_VHOST="/"
    $env:RABBITMQ_ORDER_EXCHANGE="order.exchange"
    $env:RABBITMQ_ORDER_CREATED_QUEUE="order.created.queue"
    $env:RABBITMQ_ORDER_STATUS_QUEUE="order.status.queue"
    $env:RABBITMQ_ORDER_CREATED_ROUTING_KEY="order.created"
    $env:RABBITMQ_ORDER_STATUS_ROUTING_KEY="order.status.updated"

Linux/Mac:

    export RABBITMQ_HOST="b-12345678-1234-1234-1234-123456789012.mq.us-east-1.amazonaws.com"
    export RABBITMQ_PORT="5671"
    export RABBITMQ_USERNAME="admin"
    export RABBITMQ_PASSWORD="YourSecurePassword123!"
    export RABBITMQ_SSL_ENABLED="true"

### Step 3: Deploy Application

Build for production:
mvn clean package -DskipTests

Run Publisher with dev profile:
java -jar publisher-service/target/publisher-service-1.0.0.jar --spring.profiles.active=dev

Run Listener with dev profile (new terminal):
java -jar listener-service/target/listener-service-1.0.0.jar --spring.profiles.active=dev

### Step 4: Verify AWS Deployment

Test AWS endpoint (replace with your EC2 IP or load balancer):

    curl -X POST http://your-ec2-ip:8081/api/v1/orders \
      -H "Content-Type: application/json" \
      -d '{
        "customerId": "AWS-CUST001",
        "productId": "AWS-PROD001",
        "quantity": 5,
        "totalAmount": 999.99
      }'

---

## Troubleshooting

### Issue 1: Connection Refused

Symptom:
org.springframework.amqp.AmqpConnectException: java.net.ConnectException: Connection refused

Solution:
Check if RabbitMQ is running:
docker ps

If not running, start it:
docker-compose up -d

Check logs:
docker logs myorder-rabbitmq

Verify port 5672 is not blocked:
netstat -an | findstr 5672

### Issue 2: Authentication Failure

Symptom:
ACCESS_REFUSED - Login was refused using authentication mechanism PLAIN

Solution:
Reset RabbitMQ container:
docker-compose down
docker-compose up -d

Or check credentials in application-local.yml match docker-compose.yml

### Issue 3: SSL Handshake Error (AWS)

Symptom:
javax.net.ssl.SSLHandshakeException: Remote host terminated the handshake

Solution:
- Verify RABBITMQ_SSL_ENABLED=true for AWS
- Check security group allows port 5671
- Ensure you're using port 5671 (not 5672) for AWS

### Issue 4: Queue Not Found

Symptom:
404 NOT_FOUND - no queue 'order.created.queue' in vhost '/'

Solution:
- Restart services (queues are auto-created on startup)
- Check RabbitMQ user has permissions:
  docker exec myorder-rabbitmq rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

### Issue 5: Messages Not Being Consumed

Symptom:
Queue has messages but listener not processing

Solution:
- Check listener service is running
- Verify queue names match exactly (case-sensitive)
- Check listener logs for errors
- Verify spring.profiles.active is set correctly

---

## API Reference

### Publisher Service API

#### 1. Create Order

Endpoint: POST /api/v1/orders

    Request Body:
    {
      "customerId": "string (required)",
      "productId": "string (required)",
      "quantity": "integer (required, min: 1)",
      "totalAmount": "number (required, min: 0.01)"
    }
    
    Response:
    200 OK: Order created successfully with ID: {uuid}

Example:

    curl -X POST http://localhost:8081/api/v1/orders \
      -H "Content-Type: application/json" \
      -d '{
        "customerId": "CUST001",
        "productId": "PROD001",
        "quantity": 2,
        "totalAmount": 199.99
      }'

#### 2. Update Order Status

Endpoint: POST /api/v1/orders/{orderId}/status?status={status}

Path Parameters:
- orderId: Order identifier (string)

Query Parameters:
- status: New status (CREATED, PROCESSING, COMPLETED, CANCELLED)

Response:
200 OK: Order status update published for ID: {orderId}

Example:
curl -X POST "http://localhost:8081/api/v1/orders/ORDER-123/status?status=PROCESSING"

### Health Check Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| /actuator/health | GET | Service health status |
| /actuator/info | GET | Application info |
| /actuator/metrics | GET | Performance metrics |

---

## Complete File Checklist

Ensure you have created ALL these files:

Root Level:
[x] pom.xml
[x] docker-compose.yml
[x] README.md (this file)

Publisher Service:
- [ ] publisher-service/pom.xml
- [ ] publisher-service/src/main/java/com/myorder/publisher/PublisherServiceApplication.java
- [ ] publisher-service/src/main/java/com/myorder/publisher/config/RabbitMQConfig.java
- [ ] publisher-service/src/main/java/com/myorder/publisher/controller/OrderController.java
- [ ] publisher-service/src/main/java/com/myorder/publisher/dto/OrderEvent.java
- [ ] publisher-service/src/main/java/com/myorder/publisher/service/OrderPublisherService.java
- [ ] publisher-service/src/main/resources/application.yml
- [ ] publisher-service/src/main/resources/application-local.yml
- [ ] publisher-service/src/main/resources/application-dev.yml

Listener Service:

 - [ ] listener-service/pom.xml
 - [ ] listener-service/src/main/java/com/myorder/listener/ListenerServiceApplication.java
 - [ ] listener-service/src/main/java/com/myorder/listener/config/RabbitMQConfig.java
 - [ ] listener-service/src/main/java/com/myorder/listener/dto/OrderEvent.java
 - [ ] listener-service/src/main/java/com/myorder/listener/service/OrderListenerService.java
 - [ ] listener-service/src/main/resources/application.yml
- [ ] listener-service/src/main/resources/application-local.yml
 - [ ] listener-service/src/main/resources/application-dev.yml

---

## Quick Command Reference

BUILD:

    mvn clean install                    (Build all modules)
    mvn clean package -DskipTests       (Build without tests)

RUN LOCAL:

    docker-compose up -d                (Start RabbitMQ)
    mvn spring-boot:run -Dspring-boot.run.profiles=local  (Run with local profile)

RUN AWS:

    java -jar target/app.jar --spring.profiles.active=dev (Run with dev profile)

TEST:

    curl http://localhost:8081/actuator/health           (Health check)
    curl -X POST http://localhost:8081/api/v1/orders ... (Create order)

DOCKER:

    docker ps                           (List containers)
    docker logs myorder-rabbitmq        (View RabbitMQ logs)
    docker-compose down                 (Stop infrastructure)
    docker-compose down -v              (Stop and remove data)

---

End of Documentation

For issues or contributions, please refer to the project repository or raise PR.

         
