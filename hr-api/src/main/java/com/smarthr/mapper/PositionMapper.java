package com.smarthr.mapper;

import com.smarthr.dto.PositionDTO;
import com.smarthr.dto.PositionSaveRequest;
import com.smarthr.entity.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public PositionDTO toDTO(Position position) {
        if (position == null) {
            return null;
        }

        PositionDTO dto = new PositionDTO();
        dto.setId(position.getId());
        dto.setTitle(position.getTitle());
        dto.setDescription(position.getDescription());
        dto.setSkills(position.getSkills());
        dto.setMinSalary(position.getMinSalary());
        dto.setMaxSalary(position.getMaxSalary());

        return dto;
    }

    public void updateEntity(PositionSaveRequest request, Position position) {
        if (request == null || position == null) {
            return;
        }

        position.setTitle(request.getTitle());
        position.setDescription(request.getDescription());
        position.setSkills(request.getSkills());
        position.setMinSalary(request.getMinSalary());
        position.setMaxSalary(request.getMaxSalary());
    }
}
