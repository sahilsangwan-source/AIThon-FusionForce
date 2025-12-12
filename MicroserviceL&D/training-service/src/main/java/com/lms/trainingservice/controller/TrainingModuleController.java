package com.lms.trainingservice.controller;

import com.lms.trainingservice.dto.TrainingModuleRequest;
import com.lms.trainingservice.dto.TrainingModuleResponse;
import com.lms.trainingservice.service.TrainingModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Training Module APIs
 */
@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class TrainingModuleController {

    private final TrainingModuleService trainingModuleService;

    @PostMapping
    public ResponseEntity<TrainingModuleResponse> createModule(@Valid @RequestBody TrainingModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingModuleService.createModule(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingModuleResponse> getModule(@PathVariable UUID id) {
        return ResponseEntity.ok(trainingModuleService.getModuleById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingModuleResponse> updateModule(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingModuleRequest request) {
        return ResponseEntity.ok(trainingModuleService.updateModule(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable UUID id) {
        trainingModuleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/training/{trainingId}")
    public ResponseEntity<List<TrainingModuleResponse>> getModulesByTraining(@PathVariable UUID trainingId) {
        return ResponseEntity.ok(trainingModuleService.getModulesByTraining(trainingId));
    }

}
