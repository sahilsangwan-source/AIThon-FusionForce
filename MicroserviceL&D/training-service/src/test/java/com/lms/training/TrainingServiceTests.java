package com.lms.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lms.trainingservice.controller.TrainingController;
import com.lms.trainingservice.dto.TrainingRequest;
import com.lms.trainingservice.dto.TrainingResponse;
import com.lms.trainingservice.service.TrainingService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@SpringBootTest(classes = com.lms.trainingservice.TrainingServiceApplication.class)
class TrainingServiceTests {

    @Mock
    private TrainingService trainingService;

    @Mock
    private HttpServletRequest httpRequest;

    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainingController = new TrainingController(trainingService);
    }

    @Test
    void contextLoads() {
        // Test if the application context loads successfully
    }

    @Test
    void testGetTrainingById() {
        // Arrange
        UUID trainingId = UUID.randomUUID();
        TrainingResponse mockResponse = TrainingResponse.builder()
            .id(trainingId)
            .title("Mock Training")
            .description("Description")
            .category("Category")
            .difficultyLevel("Beginner")
            .build();
        when(trainingService.getTrainingById(trainingId)).thenReturn(mockResponse);

        // Act
        ResponseEntity<TrainingResponse> response = trainingController.getTrainingById(trainingId);

        // Assert
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(trainingService, times(1)).getTrainingById(trainingId);
    }

    @Test
    void testCreateTraining() {
        // Arrange
        TrainingRequest trainingRequest = TrainingRequest.builder()
            .title("New Training")
            .description("Description")
            .category("Category")
            .difficultyLevel("Beginner")
            .build();
        UUID trainerId = UUID.randomUUID();
        TrainingResponse mockResponse = TrainingResponse.builder()
            .id(UUID.randomUUID())
            .title("New Training")
            .description("Description")
            .category("Category")
            .difficultyLevel("Beginner")
            .build();
        when(httpRequest.getAttribute("userId")).thenReturn(trainerId.toString());
        when(trainingService.createTraining(trainingRequest, trainerId)).thenReturn(mockResponse);

        // Act
        ResponseEntity<TrainingResponse> response = trainingController.createTraining(trainingRequest, httpRequest);

        // Assert
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(trainingService, times(1)).createTraining(trainingRequest, trainerId);
    }
}
