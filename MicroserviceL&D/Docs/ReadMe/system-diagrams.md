# Learning Portal - System Diagrams

This document contains visual representations of the system architecture using Mermaid diagrams.

---

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Browser]
        MOBILE[Mobile App]
    end
    
    subgraph "Load Balancing"
        LB[Nginx Load Balancer]
    end
    
    subgraph "API Gateway Layer"
        GW1[API Gateway 1]
        GW2[API Gateway 2]
        GW3[API Gateway 3]
    end
    
    subgraph "Service Discovery"
        EUREKA[Eureka Server]
    end
    
    subgraph "Microservices"
        USER[User Service]
        TRAIN[Training Service]
        PROG[Progress Service]
        ASSIGN[Assignment Service]
        NOTIF[Notification Service]
        CONTENT[Content Service]
        ANALYTICS[Analytics Service]
        WORKFLOW[Workflow Service]
    end
    
    subgraph "Message Broker"
        KAFKA[Apache Kafka]
    end
    
    subgraph "Cache Layer"
        REDIS[Redis Cluster]
    end
    
    subgraph "Data Layer"
        PG_MASTER[PostgreSQL Master]
        PG_REPLICA1[PostgreSQL Replica 1]
        PG_REPLICA2[PostgreSQL Replica 2]
        MINIO[MinIO Object Storage]
    end
    
    subgraph "Monitoring"
        PROM[Prometheus]
        GRAF[Grafana]
        ELK[ELK Stack]
    end
    
    WEB --> LB
    MOBILE --> LB
    LB --> GW1
    LB --> GW2
    LB --> GW3
    
    GW1 --> EUREKA
    GW2 --> EUREKA
    GW3 --> EUREKA
    
    GW1 --> USER
    GW1 --> TRAIN
    GW1 --> PROG
    
    USER --> KAFKA
    TRAIN --> KAFKA
    PROG --> KAFKA
    ASSIGN --> KAFKA
    NOTIF --> KAFKA
    
    USER --> REDIS
    TRAIN --> REDIS
    PROG --> REDIS
    
    USER --> PG_MASTER
    TRAIN --> PG_MASTER
    PROG --> PG_MASTER
    ASSIGN --> PG_MASTER
    
    PG_MASTER --> PG_REPLICA1
    PG_MASTER --> PG_REPLICA2
    
    CONTENT --> MINIO
    
    PROM --> GW1
    PROM --> USER
    PROM --> TRAIN
    GRAF --> PROM
```

---

## 2. Microservices Communication Flow

```mermaid
sequenceDiagram
    participant Admin
    participant Gateway
    participant Assignment
    participant Kafka
    participant Progress
    participant Notification
    participant User
    
    Admin->>Gateway: Assign Training to 1000 users
    Gateway->>Assignment: POST /api/assignments/bulk
    Assignment->>Assignment: Validate request
    Assignment->>Kafka: Publish training.assigned event
    Assignment->>Gateway: Return 202 Accepted
    Gateway->>Admin: Success response
    
    Kafka->>Progress: Consume event
    Progress->>Progress: Create 1000 assignments
    Progress->>Kafka: Publish assignment.created
    
    Kafka->>Notification: Consume event
    Notification->>Notification: Send 1000 notifications
    Notification->>User: Email notification
    Notification->>User: Push notification
    Notification->>User: In-app notification
```

---

## 3. Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Gateway
    participant UserService
    participant Redis
    participant DB
    
    User->>Frontend: Enter credentials
    Frontend->>Gateway: POST /api/users/login
    Gateway->>UserService: Forward request
    UserService->>DB: Verify credentials
    DB->>UserService: User data
    UserService->>UserService: Generate JWT tokens
    UserService->>Redis: Store session
    UserService->>Gateway: Return tokens
    Gateway->>Frontend: JWT tokens
    Frontend->>User: Login successful
    
    Note over User,DB: Subsequent requests
    
    User->>Frontend: Access protected resource
    Frontend->>Gateway: GET /api/trainings<br/>Authorization: Bearer token
    Gateway->>Redis: Validate token
    Redis->>Gateway: Token valid
    Gateway->>UserService: Forward request
    UserService->>Frontend: Return data
    Frontend->>User: Display data
```

---

## 4. SSO Authentication Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Gateway
    participant UserService
    participant IDP as Identity Provider
    participant DB
    
    User->>Frontend: Click Login with SSO
    Frontend->>Gateway: Initiate SSO
    Gateway->>IDP: Redirect to IDP
    IDP->>User: Show login page
    User->>IDP: Enter credentials
    IDP->>IDP: Authenticate
    IDP->>Gateway: Redirect with auth code
    Gateway->>UserService: Exchange code for tokens
    UserService->>IDP: POST /token
    IDP->>UserService: Access token + ID token
    UserService->>DB: Create/Update user
    UserService->>UserService: Generate internal JWT
    UserService->>Gateway: Return JWT
    Gateway->>Frontend: JWT token
    Frontend->>User: Login successful
```

---

## 5. Training Assignment Event Flow

```mermaid
graph LR
    subgraph "Event Publishers"
        ASSIGN[Assignment Service]
        PROG[Progress Service]
        USER[User Service]
    end
    
    subgraph "Kafka Topics"
        T1[training.assigned]
        T2[training.completed]
        T3[user.role.changed]
    end
    
    subgraph "Event Consumers"
        NOTIF[Notification Service]
        ANALYTICS[Analytics Service]
        WORKFLOW[Workflow Service]
    end
    
    ASSIGN -->|Publish| T1
    PROG -->|Publish| T2
    USER -->|Publish| T3
    
    T1 -->|Consume| NOTIF
    T1 -->|Consume| PROG
    T1 -->|Consume| ANALYTICS
    
    T2 -->|Consume| NOTIF
    T2 -->|Consume| ANALYTICS
    T2 -->|Consume| WORKFLOW
    
    T3 -->|Consume| ASSIGN
```

---

## 6. Progress Tracking Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant Gateway
    participant Progress
    participant Kafka
    participant Redis
    participant DB
    participant Analytics
    
    User->>Frontend: Watch video
    loop Every 30 seconds
        Frontend->>Gateway: POST /api/progress/update
        Gateway->>Progress: Forward request
        Progress->>Redis: Update cache
        Progress->>Kafka: Publish progress.updated
        Progress->>Gateway: ACK
        Gateway->>Frontend: Success
    end
    
    User->>Frontend: Complete video
    Frontend->>Gateway: POST /api/progress/complete
    Gateway->>Progress: Forward request
    Progress->>DB: Mark complete
    Progress->>Kafka: Publish training.completed
    Progress->>Gateway: Success
    Gateway->>Frontend: Completion confirmed
    
    Kafka->>Analytics: Consume event
    Analytics->>Analytics: Update statistics
    Analytics->>Analytics: Generate certificate
```

---

## 7. Caching Strategy

```mermaid
graph TB
    subgraph "Request Flow"
        CLIENT[Client Request]
        GATEWAY[API Gateway]
        SERVICE[Microservice]
    end
    
    subgraph "Cache Layers"
        L1[Browser Cache]
        L2[CDN Cache]
        L3[Redis Cache]
        L4[Application Cache]
    end
    
    subgraph "Data Sources"
        DB[(Database)]
        S3[Object Storage]
    end
    
    CLIENT -->|1. Check| L1
    L1 -->|Miss| L2
    L2 -->|Miss| GATEWAY
    GATEWAY -->|2. Check| L3
    L3 -->|Miss| SERVICE
    SERVICE -->|3. Check| L4
    L4 -->|Miss| DB
    
    DB -->|Data| L4
    L4 -->|Data| L3
    L3 -->|Data| GATEWAY
    GATEWAY -->|Data| L2
    L2 -->|Data| CLIENT
```

---

## 8. Database Architecture

```mermaid
graph TB
    subgraph "Application Layer"
        APP1[User Service]
        APP2[Training Service]
        APP3[Progress Service]
    end
    
    subgraph "Database Cluster"
        MASTER[(PostgreSQL Master<br/>Read + Write)]
        REPLICA1[(Replica 1<br/>Read Only)]
        REPLICA2[(Replica 2<br/>Read Only)]
    end
    
    subgraph "Backup"
        BACKUP[Automated Backups]
        S3_BACKUP[S3 Backup Storage]
    end
    
    APP1 -->|Write| MASTER
    APP2 -->|Write| MASTER
    APP3 -->|Write| MASTER
    
    APP1 -->|Read| REPLICA1
    APP2 -->|Read| REPLICA2
    APP3 -->|Read| REPLICA1
    
    MASTER -->|Replicate| REPLICA1
    MASTER -->|Replicate| REPLICA2
    
    MASTER -->|Daily| BACKUP
    BACKUP -->|Store| S3_BACKUP
```

---

## 9. Content Delivery Architecture

```mermaid
graph LR
    subgraph "Users"
        USER1[User 1]
        USER2[User 2]
        USER3[User 3]
    end
    
    subgraph "CDN Layer"
        CDN[CloudFront CDN]
        EDGE1[Edge Location 1]
        EDGE2[Edge Location 2]
    end
    
    subgraph "Origin"
        CONTENT[Content Service]
        MINIO[MinIO/S3]
    end
    
    USER1 -->|Request video| EDGE1
    USER2 -->|Request video| EDGE2
    USER3 -->|Request video| EDGE1
    
    EDGE1 -->|Cache miss| CDN
    EDGE2 -->|Cache miss| CDN
    
    CDN -->|Fetch| CONTENT
    CONTENT -->|Get file| MINIO
    
    MINIO -->|File| CONTENT
    CONTENT -->|File| CDN
    CDN -->|Cached| EDGE1
    CDN -->|Cached| EDGE2
```

---

## 10. Monitoring Architecture

```mermaid
graph TB
    subgraph "Services"
        SVC1[User Service]
        SVC2[Training Service]
        SVC3[Progress Service]
        GATEWAY[API Gateway]
    end
    
    subgraph "Metrics Collection"
        PROM[Prometheus]
    end
    
    subgraph "Visualization"
        GRAF[Grafana]
    end
    
    subgraph "Logging"
        LOGSTASH[Logstash]
        ES[Elasticsearch]
        KIBANA[Kibana]
    end
    
    subgraph "Alerting"
        ALERT[Alert Manager]
        SLACK[Slack]
        EMAIL[Email]
    end
    
    SVC1 -->|Metrics| PROM
    SVC2 -->|Metrics| PROM
    SVC3 -->|Metrics| PROM
    GATEWAY -->|Metrics| PROM
    
    PROM -->|Query| GRAF
    PROM -->|Alerts| ALERT
    
    SVC1 -->|Logs| LOGSTASH
    SVC2 -->|Logs| LOGSTASH
    SVC3 -->|Logs| LOGSTASH
    
    LOGSTASH -->|Index| ES
    ES -->|Query| KIBANA
    
    ALERT -->|Notify| SLACK
    ALERT -->|Notify| EMAIL
```

---

## 11. Deployment Architecture

```mermaid
graph TB
    subgraph "Development"
        DEV[Developer]
        GIT[Git Repository]
    end
    
    subgraph "CI/CD Pipeline"
        JENKINS[Jenkins/GitHub Actions]
        BUILD[Build & Test]
        DOCKER[Docker Build]
        REGISTRY[Docker Registry]
    end
    
    subgraph "Kubernetes Cluster"
        MASTER[K8s Master]
        NODE1[Worker Node 1]
        NODE2[Worker Node 2]
        NODE3[Worker Node 3]
    end
    
    subgraph "Monitoring"
        PROM[Prometheus]
        GRAF[Grafana]
    end
    
    DEV -->|Push code| GIT
    GIT -->|Trigger| JENKINS
    JENKINS -->|Run| BUILD
    BUILD -->|Success| DOCKER
    DOCKER -->|Push| REGISTRY
    REGISTRY -->|Deploy| MASTER
    
    MASTER -->|Schedule pods| NODE1
    MASTER -->|Schedule pods| NODE2
    MASTER -->|Schedule pods| NODE3
    
    NODE1 -->|Metrics| PROM
    NODE2 -->|Metrics| PROM
    NODE3 -->|Metrics| PROM
    
    PROM -->|Visualize| GRAF
```

---

## 12. Kafka Topic Architecture

```mermaid
graph TB
    subgraph "Kafka Cluster"
        BROKER1[Broker 1]
        BROKER2[Broker 2]
        BROKER3[Broker 3]
    end
    
    subgraph "Topics"
        T1[training.assigned<br/>10 partitions]
        T2[progress.updated<br/>20 partitions]
        T3[training.completed<br/>10 partitions]
        T4[notification.events<br/>15 partitions]
    end
    
    subgraph "Producers"
        P1[Assignment Service]
        P2[Progress Service]
    end
    
    subgraph "Consumers"
        C1[Notification Service<br/>Consumer Group 1]
        C2[Analytics Service<br/>Consumer Group 2]
        C3[Progress Service<br/>Consumer Group 3]
    end
    
    P1 -->|Publish| T1
    P2 -->|Publish| T2
    P2 -->|Publish| T3
    
    T1 -->|Partition 0-9| BROKER1
    T2 -->|Partition 0-19| BROKER2
    T3 -->|Partition 0-9| BROKER3
    
    BROKER1 -->|Consume| C1
    BROKER2 -->|Consume| C2
    BROKER3 -->|Consume| C3
```

---

## 13. Redis Cluster Architecture

```mermaid
graph TB
    subgraph "Redis Cluster"
        M1[Master 1<br/>Slots 0-5460]
        M2[Master 2<br/>Slots 5461-10922]
        M3[Master 3<br/>Slots 10923-16383]
        
        S1[Slave 1]
        S2[Slave 2]
        S3[Slave 3]
    end
    
    subgraph "Applications"
        APP1[User Service]
        APP2[Training Service]
        APP3[Progress Service]
    end
    
    M1 -.->|Replicate| S1
    M2 -.->|Replicate| S2
    M3 -.->|Replicate| S3
    
    APP1 -->|Read/Write| M1
    APP2 -->|Read/Write| M2
    APP3 -->|Read/Write| M3
    
    APP1 -->|Read| S1
    APP2 -->|Read| S2
    APP3 -->|Read| S3
```

---

## 14. Report Generation Flow

```mermaid
sequenceDiagram
    participant Admin
    participant Frontend
    participant Gateway
    participant Analytics
    participant Celery
    participant DB
    participant S3
    participant Kafka
    
    Admin->>Frontend: Request report
    Frontend->>Gateway: POST /api/reports/generate
    Gateway->>Analytics: Forward request
    Analytics->>Celery: Create async task
    Analytics->>Gateway: Return task_id
    Gateway->>Frontend: 202 Accepted + task_id
    Frontend->>Admin: Report generation started
    
    Celery->>DB: Fetch data
    DB->>Celery: Return data
    Celery->>Celery: Process with Pandas
    Celery->>Celery: Generate Excel
    Celery->>S3: Upload report
    S3->>Celery: Return URL
    Celery->>Kafka: Publish report.generated
    
    Kafka->>Analytics: Consume event
    Analytics->>Frontend: WebSocket notification
    Frontend->>Admin: Report ready for download
```

---

## 15. Scaling Strategy

```mermaid
graph TB
    subgraph "Load Monitoring"
        METRICS[Prometheus Metrics]
        HPA[Horizontal Pod Autoscaler]
    end
    
    subgraph "Current State"
        POD1[Pod 1]
        POD2[Pod 2]
        POD3[Pod 3]
    end
    
    subgraph "Scaled State"
        POD4[Pod 4]
        POD5[Pod 5]
        POD6[Pod 6]
    end
    
    METRICS -->|CPU > 70%| HPA
    HPA -->|Scale up| POD4
    HPA -->|Scale up| POD5
    HPA -->|Scale up| POD6
    
    POD1 -.->|Load decreases| HPA
    HPA -.->|Scale down| POD6
```

---

## 16. Circuit Breaker Pattern

```mermaid
stateDiagram-v2
    [*] --> Closed
    Closed --> Open: Failure threshold reached
    Open --> HalfOpen: Timeout expires
    HalfOpen --> Closed: Success
    HalfOpen --> Open: Failure
    
    note right of Closed
        Normal operation
        Requests pass through
    end note
    
    note right of Open
        Requests fail fast
        Return cached data
    end note
    
    note right of HalfOpen
        Test if service recovered
        Limited requests allowed
    end note
```

---

## 17. Data Consistency Pattern

```mermaid
sequenceDiagram
    participant Client
    participant Service1
    participant Kafka
    participant Service2
    participant Service3
    participant DB
    
    Client->>Service1: Create order
    Service1->>DB: Save order
    Service1->>Kafka: Publish order.created
    Service1->>Client: Return success
    
    Kafka->>Service2: Consume event
    Service2->>DB: Update inventory
    Service2->>Kafka: Publish inventory.updated
    
    Kafka->>Service3: Consume event
    Service3->>DB: Update analytics
    
    Note over Service1,Service3: Eventual Consistency
```

---

## 18. Security Layers

```mermaid
graph TB
    subgraph "External"
        USER[User]
        ATTACKER[Potential Attacker]
    end
    
    subgraph "Security Layers"
        WAF[Web Application Firewall]
        LB[Load Balancer + SSL/TLS]
        GATEWAY[API Gateway<br/>Rate Limiting]
        AUTH[Authentication<br/>JWT Validation]
        AUTHZ[Authorization<br/>RBAC]
    end
    
    subgraph "Services"
        SVC[Microservices]
    end
    
    USER -->|HTTPS| WAF
    ATTACKER -->|Blocked| WAF
    WAF -->|Filter| LB
    LB -->|Secure| GATEWAY
    GATEWAY -->|Validate| AUTH
    AUTH -->|Check| AUTHZ
    AUTHZ -->|Allowed| SVC
```

This comprehensive set of diagrams provides visual representations of all major architectural components and flows in the Learning & Development Portal system.
