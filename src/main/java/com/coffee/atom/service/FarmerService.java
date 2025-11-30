package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.dto.FarmerResponseDto;
import com.coffee.atom.dto.FarmersResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmerService {
    private final FarmerRepository farmerRepository;

    @Transactional(readOnly = true)
    public List<FarmersResponseDto> getFarmersWithVillageHeadAndSection(AppUser currentUser) {
        Role role = currentUser.getRole();
        switch (role) {
            case ADMIN -> {
                return farmerRepository.findAllApprovedFarmersWithVillageHeadAndSection();
            }
            case VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER -> {
                if (currentUser.getArea() == null) {
                    throw new CustomException(ErrorValue.AREA_NOT_FOUND);
                }
                return farmerRepository.findAllByAreaId(currentUser.getArea().getId());
            }
            case VILLAGE_HEAD -> {
                return farmerRepository.findAllByVillageHeadId(currentUser.getId());
            }
            default -> throw new CustomException(ErrorValue.ROLE_NOT_ALLOWED_FARMER_LIST);
        }
    }

    @Transactional(readOnly = true)
    public FarmerResponseDto getFarmerDetail(Long farmerId) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new CustomException(ErrorValue.FARMER_NOT_FOUND));

        String sectionName = farmer.getVillageHead().getSection() != null 
                ? farmer.getVillageHead().getSection().getSectionName() 
                : null;

        return FarmerResponseDto.builder()
                .sectionName(sectionName)
                .farmerName(farmer.getName())
                .identificationPhotoUrl(farmer.getIdentificationPhotoUrl())
                .build();
    }

}
