package com.smarthr.controller;

import com.smarthr.dto.PositionDTO;
import com.smarthr.dto.PositionSaveRequest;
import com.smarthr.service.PositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<PositionDTO>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<PositionDTO> getPositionById(@PathVariable UUID id) {
        return ResponseEntity.ok(positionService.getPositionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<PositionDTO> createPosition(@Valid @RequestBody PositionSaveRequest request) {
        PositionDTO created = positionService.createPosition(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<PositionDTO> updatePosition(
            @PathVariable UUID id, @Valid @RequestBody PositionSaveRequest request) {
        return ResponseEntity.ok(positionService.updatePosition(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePosition(@PathVariable UUID id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}
