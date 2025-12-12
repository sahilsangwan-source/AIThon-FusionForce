package com.lms.trainingservice.controller;

import com.lms.trainingservice.dto.TrainingContentRequest;
import com.lms.trainingservice.dto.TrainingContentResponse;
import com.lms.trainingservice.service.TrainingContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Training Content APIs
 */
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class TrainingContentController {

    private final TrainingContentService trainingContentService;

    @PostMapping
    public ResponseEntity<TrainingContentResponse> createContent(@Valid @RequestBody TrainingContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingContentService.createContent(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingContentResponse> getContent(@PathVariable UUID id) {
        return ResponseEntity.ok(trainingContentService.getContentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingContentResponse> updateContent(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingContentRequest request) {
        return ResponseEntity.ok(trainingContentService.updateContent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable UUID id) {
        trainingContentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<TrainingContentResponse>> getContentByModule(@PathVariable UUID moduleId) {
        return ResponseEntity.ok(trainingContentService.getContentByModule(moduleId));
    }

}
