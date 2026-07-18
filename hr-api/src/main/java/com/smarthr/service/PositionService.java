package com.smarthr.service;

import com.smarthr.dto.PositionDTO;
import com.smarthr.dto.PositionSaveRequest;

import java.util.List;
import java.util.UUID;

public interface PositionService {
    List<PositionDTO> getAllPositions();
    PositionDTO getPositionById(UUID id);
    PositionDTO createPosition(PositionSaveRequest request);
    PositionDTO updatePosition(UUID id, PositionSaveRequest request);
    void deletePosition(UUID id);
}
