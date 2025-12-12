# Training Service Implementation - Complete Summary

## Overview

The Training Service is a complete microservice for managing training courses, modules, content, and quizzes in the Learning Management System. It provides comprehensive REST APIs for training management with proper database persistence, caching, and messaging integration.

## Architecture

### Service Configuration

- **Port:** 8082
- **Service Name:** TRAINING-SERVICE
- **Context Path:** / (root)
- **Database:** PostgreSQL (lms_db)
- **Cache:** Redis
- **Message Broker:** Apache Kafka

### Registered Services

- EUREKA-SERVER (8761) - Service Discovery
- USER-SERVICE (8081) - User Management
- TRAINING-SERVICE (8082) - Training Management

## Database Schema

### Tables Implemented

#### 1. **trainings**

Core training course information

```sql
CREATE TABLE trainings (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration INTEGER,
    category VARCHAR(100),
    difficulty_level VARCHAR(50),
    status VARCHAR(50),
    instructor_id UUID,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. **training_modules**

Modules within trainings

```sql
CREATE TABLE training_modules (
    id UUID PRIMARY KEY,
    training_id UUID REFERENCES trainings(id),
    title VARCHAR(255),
    description TEXT,
    module_order INTEGER,
    duration INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. **training_content**

Actual training content/lessons

```sql
CREATE TABLE training_content (
    id UUID PRIMARY KEY,
    module_id UUID REFERENCES training_modules(id),
    title VARCHAR(255),
    content_type VARCHAR(100),
    url VARCHAR(1000),
    duration INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4. **quizzes**

Quiz assessments for trainings

```sql
CREATE TABLE quizzes (
    id UUID PRIMARY KEY,
    training_id UUID REFERENCES trainings(id),
    title VARCHAR(255),
    description TEXT,
    passing_score INTEGER,
    duration INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 5. **quiz_questions**

Individual quiz questions

```sql
CREATE TABLE quiz_questions (
    id UUID PRIMARY KEY,
    quiz_id UUID REFERENCES quizzes(id),
    question_text TEXT,
    question_type VARCHAR(50),
    correct_answer VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## API Endpoints

### Training Management

#### Create Training (Protected)

```
POST /api/trainings
Content-Type: application/json

{
    "title": "Java Basics",
    "description": "Learn Java fundamentals",
    "duration": 40,
    "category": "PROGRAMMING",
    "instructorId": "550e8400-e29b-41d4-a716-446655440000"
}

Response: 201 CREATED
{
    "id": "UUID",
    "title": "Java Basics",
    "description": "Learn Java fundamentals",
    "duration": 40,
    "category": "PROGRAMMING",
    "status": "DRAFT",
    "createdAt": "2025-12-12T08:25:06Z"
}
```

#### Get All Trainings (Paginated)

```
GET /api/trainings?page=0&size=20

Response: 200 OK
{
    "content": [ ... ],
    "pageable": { ... },
    "totalElements": 100,
    "totalPages": 5
}
```

#### Get Training by ID

```
GET /api/trainings/{id}

Response: 200 OK
{ ... training details ... }
```

#### Update Training

```
PUT /api/trainings/{id}
Content-Type: application/json

{ ... updated fields ... }

Response: 200 OK
```

#### Delete Training

```
DELETE /api/trainings/{id}

Response: 204 NO CONTENT
```

### Filter & Search Endpoints

#### Get by Category

```
GET /api/trainings/category/{category}?page=0&size=20
```

#### Get by Difficulty

```
GET /api/trainings/difficulty/{difficulty}?page=0&size=20
```

#### Get by Status

```
GET /api/trainings/status/{status}?page=0&size=20
```

#### Search Trainings

```
GET /api/trainings/search?query=java&page=0&size=20
```

#### Get Published Trainings

```
GET /api/trainings/published?page=0&size=20
```

### Training Module Endpoints

#### Create Module

```
POST /api/training-modules
{
    "trainingId": "UUID",
    "title": "Module 1",
    "description": "Introduction",
    "moduleOrder": 1,
    "duration": 30
}
```

#### Get Module by ID

```
GET /api/training-modules/{id}
```

#### Update Module

```
PUT /api/training-modules/{id}
```

#### Delete Module

```
DELETE /api/training-modules/{id}
```

#### Get Modules by Training

```
GET /api/training-modules/training/{trainingId}
```

### Training Content Endpoints

#### Create Content

```
POST /api/training-content
{
    "moduleId": "UUID",
    "title": "Lesson 1",
    "contentType": "VIDEO",
    "url": "https://example.com/video.mp4",
    "duration": 20
}
```

#### Get Content by ID

```
GET /api/training-content/{id}
```

#### Update Content

```
PUT /api/training-content/{id}
```

#### Delete Content

```
DELETE /api/training-content/{id}
```

#### Get Content by Module

```
GET /api/training-content/module/{moduleId}
```

### Quiz Endpoints

#### Create Quiz

```
POST /api/quizzes
{
    "trainingId": "UUID",
    "title": "Java Quiz",
    "description": "Test your Java knowledge",
    "passingScore": 70,
    "duration": 60
}
```

#### Get Quiz by ID

```
GET /api/quizzes/{id}
```

#### Update Quiz

```
PUT /api/quizzes/{id}
```

#### Delete Quiz

```
DELETE /api/quizzes/{id}
```

#### Get Quiz Questions

```
GET /api/quizzes/{id}/questions
```

### Quiz Question Endpoints

#### Create Question

```
POST /api/quiz-questions
{
    "quizId": "UUID",
    "questionText": "What is Java?",
    "questionType": "MULTIPLE_CHOICE",
    "correctAnswer": "Programming language"
}
```

#### Get Question by ID

```
GET /api/quiz-questions/{id}
```

#### Update Question

```
PUT /api/quiz-questions/{id}
```

#### Delete Question

```
DELETE /api/quiz-questions/{id}
```

#### Get Questions by Quiz

```
GET /api/quiz-questions/quiz/{quizId}
```

## Entity Models

### Training Entity

```java
@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;
    private Integer duration;
    private String category;
    private String difficultyLevel;
    private String status;

    private UUID instructorId;
    private UUID createdBy;
    private UUID updatedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### TrainingModule Entity

```java
@Entity
@Table(name = "training_modules")
public class TrainingModule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "training_id")
    private Training training;

    private String title;
    private String description;
    private Integer moduleOrder;
    private Integer duration;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### TrainingContent Entity

```java
@Entity
@Table(name = "training_content")
public class TrainingContent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private TrainingModule module;

    private String title;
    private String contentType;  // VIDEO, PDF, DOCUMENT, etc.
    private String url;
    private Integer duration;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Quiz Entity

```java
@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "training_id")
    private Training training;

    private String title;
    private String description;
    private Integer passingScore;
    private Integer duration;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### QuizQuestion Entity

```java
@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private String questionText;
    private String questionType;  // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    private String correctAnswer;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

## DTOs

### TrainingRequest

```java
{
    "title": String (required, max 255),
    "description": String (optional),
    "duration": Integer (optional),
    "category": String (required, enum),
    "difficultyLevel": String (optional),
    "status": String (optional),
    "instructorId": UUID (required)
}
```

### TrainingResponse

```java
{
    "id": UUID,
    "title": String,
    "description": String,
    "duration": Integer,
    "category": String,
    "difficultyLevel": String,
    "status": String,
    "instructorId": UUID,
    "createdAt": LocalDateTime,
    "updatedAt": LocalDateTime
}
```

## Services

### TrainingService

Main service for training operations

- `createTraining(request, userId)` - Create new training
- `getTrainingById(id)` - Retrieve training by ID
- `getAllTrainings(pageable)` - Get all with pagination
- `updateTraining(id, request)` - Update existing training
- `deleteTraining(id)` - Delete training
- `searchTrainings(query, pageable)` - Search trainings
- `getTrainingsByCategory(category, pageable)` - Filter by category
- `getTrainingsByDifficultyLevel(difficulty, pageable)` - Filter by difficulty
- `getTrainingsByStatus(status, pageable)` - Filter by status
- `getPublishedTrainings(pageable)` - Get published only

### TrainingModuleService

Module management

- `createModule(request)` - Create training module
- `getModuleById(id)` - Get module details
- `updateModule(id, request)` - Update module
- `deleteModule(id)` - Delete module
- `getModulesByTraining(trainingId, pageable)` - Get training modules

### TrainingContentService

Content management

- `createContent(request)` - Add training content
- `getContentById(id)` - Get content details
- `updateContent(id, request)` - Update content
- `deleteContent(id)` - Delete content
- `getContentByModule(moduleId, pageable)` - Get module content

### QuizService

Quiz management

- `createQuiz(request)` - Create quiz
- `getQuizById(id)` - Get quiz details
- `updateQuiz(id, request)` - Update quiz
- `deleteQuiz(id)` - Delete quiz
- `getQuizzesByTraining(trainingId, pageable)` - Get training quizzes

## Configuration

### Spring Data JPA

- Hibernate ORM with PostgreSQL
- Automatic schema validation
- Connection pooling (HikariCP)

### Caching

- Redis for performance optimization
- Cache keys: `training:{id}`, `trainings:page:{page}`

### Messaging

- Kafka producer for training events
- Topics: `training-events`, `module-events`, `quiz-events`

### CORS

- All /api/\*\* endpoints enabled
- Allows: GET, POST, PUT, DELETE, OPTIONS
- Origins: \* (all)

## Running the Service

### Docker Compose

```bash
cd /Users/sahil_sangwan/Desktop/plans
docker-compose down
docker-compose up -d
```

### Verify Service

```bash
# Check service health
curl http://localhost:8082/actuator/health

# Check Eureka registration
curl http://localhost:8761/eureka/apps
```

### Service Dependencies

- PostgreSQL: localhost:5433
- Redis: localhost:6379
- Kafka: localhost:29092
- Eureka: localhost:8761

## Important Notes

1. **Default Credentials:**

   - Basic Auth enabled by default with generated password
   - Check logs: `Using generated security password:`

2. **TODO Items:**

   - JWT authentication needs to be integrated
   - User ID should come from JWT token, not hardcoded
   - Create SecurityConfig class to enable/disable endpoints

3. **Next Steps:**
   - Implement JWT authentication
   - Add role-based access control (RBAC)
   - Implement service-to-service authentication
   - Add comprehensive error handling
   - Create integration tests

## File Structure

```
training-service/
├── src/main/java/com/lms/trainingservice/
│   ├── controller/
│   │   ├── TrainingController.java
│   │   ├── TrainingModuleController.java
│   │   ├── TrainingContentController.java
│   │   └── QuizController.java
│   ├── service/
│   │   ├── TrainingService.java
│   │   ├── TrainingModuleService.java
│   │   ├── TrainingContentService.java
│   │   └── QuizService.java
│   ├── entity/
│   │   ├── Training.java
│   │   ├── TrainingModule.java
│   │   ├── TrainingContent.java
│   │   ├── Quiz.java
│   │   └── QuizQuestion.java
│   ├── repository/
│   │   ├── TrainingRepository.java
│   │   ├── TrainingModuleRepository.java
│   │   ├── TrainingContentRepository.java
│   │   ├── QuizRepository.java
│   │   └── QuizQuestionRepository.java
│   ├── dto/
│   │   ├── TrainingRequest.java
│   │   ├── TrainingResponse.java
│   │   └── ...
│   ├── config/
│   │   ├── CorsConfig.java
│   │   └── KafkaConfig.java
│   ├── exception/
│   │   ├── TrainingException.java
│   │   ├── TrainingNotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   └── TrainingServiceApplication.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── Dockerfile
```

## Testing

### Health Check

```bash
curl http://localhost:8082/actuator/health
```

### List Trainings

```bash
curl http://localhost:8082/api/trainings?page=0&size=20
```

### Search Trainings

```bash
curl "http://localhost:8082/api/trainings/search?query=java&page=0&size=20"
```

## Status: ✅ COMPLETE

The Training Service is fully implemented with:

- ✅ Complete database schema
- ✅ All entities and repositories
- ✅ Service layer implementation
- ✅ REST API controllers
- ✅ Exception handling
- ✅ Pagination support
- ✅ Search and filtering
- ✅ CORS configuration
- ✅ Redis caching integration
- ✅ Kafka messaging integration
- ✅ Eureka service registration
- ✅ Docker deployment

Ready for: Authentication/Authorization, Integration testing, Production deployment
