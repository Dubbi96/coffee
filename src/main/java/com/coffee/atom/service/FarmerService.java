package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.TreesTransaction;
import com.coffee.atom.domain.TreesTransactionRepository;
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
    private final TreesTransactionRepository treesTransactionRepository;

    @Transactional(readOnly = true)
    public List<FarmersResponseDto> getFarmersWithVillageHeadAndSection(AppUser currentUser) {
        Role role = currentUser.getRole();
        switch (role) {
            case ADMIN -> {
                return farmerRepository.findAllApprovedFarmersWithVillageHeadAndSection();
            }
            case VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER -> {
                if (currentUser.getArea() == null) {
                    throw new CustomException(ErrorValue.AREA_NOT_FOUND.getMessage());
                }
                return farmerRepository.findAllByAreaId(currentUser.getArea().getId());
            }
            case VILLAGE_HEAD -> {
                return farmerRepository.findAllByVillageHeadId(currentUser.getId());
            }
            default -> throw new CustomException("해당 역할은 농부 목록을 조회할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public FarmerResponseDto getFarmerTreeTransactions(Long farmerId) {
        List<TreesTransaction> transactions = treesTransactionRepository.findAllApprovedByFarmerId(farmerId);

        List<FarmerResponseDto.TreesTransactionDetail> details = transactions.stream()
            .map(t -> FarmerResponseDto.TreesTransactionDetail.builder()
                    .species(t.getSpecies())
                    .receivedDate(t.getReceivedDate())
                    .quantity(t.getQuantity())
                    .build())
            .toList();

        //쿼리를 줄이기 위해 기존 가져온 TreesTransaction List가 있다면 농부 추출
        Farmer farmer = transactions.isEmpty() ? farmerRepository.findById(farmerId)
            .orElseThrow(() -> new CustomException("해당 농부를 찾을 수 없습니다.")) : transactions.get(0).getFarmer();

        String sectionName = farmer.getVillageHead().getSection() != null 
                ? farmer.getVillageHead().getSection().getSectionName() 
                : null;

        return FarmerResponseDto.builder()
                .sectionName(sectionName)
                .farmerName(farmer.getName())
                .identificationPhotoUrl(farmer.getIdentificationPhotoUrl())
                .treesTransactions(details)
                .build();
    }
}
