package com.lms.trainingservice.controller;

import com.lms.trainingservice.dto.TrainingRequest;
import com.lms.trainingservice.dto.TrainingResponse;
import com.lms.trainingservice.service.TrainingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * REST Controller for Training APIs
 */
@RestController
@RequestMapping("/api/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    public ResponseEntity<TrainingResponse> createTraining(
            @Valid @RequestBody TrainingRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString((String) httpRequest.getAttribute("userId"));
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.createTraining(request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingResponse> getTrainingById(@PathVariable UUID id) {
        return ResponseEntity.ok(trainingService.getTrainingById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingResponse> updateTraining(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingRequest request) {
        return ResponseEntity.ok(trainingService.updateTraining(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable UUID id) {
        trainingService.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<TrainingResponse>> getAllTrainings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingService.getAllTrainings(pageable));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<TrainingResponse>> getByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingService.getTrainingsByCategory(category, pageable));
    }

    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<Page<TrainingResponse>> getByDifficulty(
            @PathVariable String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingService.getTrainingsByDifficultyLevel(difficulty, pageable));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<TrainingResponse>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingService.getTrainingsByStatus(status, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TrainingResponse>> searchTrainings(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingService.searchTrainings(query, pageable));
    }

    @GetMapping("/published")
    public ResponseEntity<Page<TrainingResponse>> getPublishedTrainings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(trainingService.getPublishedTrainings(pageable));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<TrainingResponse> publishTraining(@PathVariable UUID id) {
        return ResponseEntity.ok(trainingService.publishTraining(id));
    }

}
