package com.smarthr.service.impl;

import com.smarthr.dto.PositionDTO;
import com.smarthr.dto.PositionSaveRequest;
import com.smarthr.entity.Position;
import com.smarthr.exception.ErrorConstants;
import com.smarthr.exception.ResourceNotFoundException;
import com.smarthr.mapper.PositionMapper;
import com.smarthr.repository.PositionRepository;
import com.smarthr.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PositionDTO> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(positionMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PositionDTO getPositionById(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.POSITION_NOT_FOUND, 
                        "Poste introuvable avec l'ID: " + id));
        return positionMapper.toDTO(position);
    }

    @Override
    @Transactional
    public PositionDTO createPosition(PositionSaveRequest request) {
        Position position = new Position();
        positionMapper.updateEntity(request, position);
        Position saved = positionRepository.save(position);
        return positionMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public PositionDTO updatePosition(UUID id, PositionSaveRequest request) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.POSITION_NOT_FOUND, 
                        "Poste introuvable avec l'ID: " + id));

        positionMapper.updateEntity(request, position);
        Position updated = positionRepository.save(position);
        return positionMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorConstants.POSITION_NOT_FOUND, 
                        "Poste introuvable avec l'ID: " + id));
        positionRepository.delete(position);
    }
}
