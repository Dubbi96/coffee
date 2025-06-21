package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.TreesTransaction;
import com.coffee.atom.domain.TreesTransactionRepository;
import com.coffee.atom.domain.appuser.*;
import com.coffee.atom.dto.TreeTransactionResponseDto;
import com.coffee.atom.dto.approval.ApprovalTreesTransactionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TreesTransactionService {

    private final TreesTransactionRepository treesTransactionRepository;
    private final FarmerRepository farmerRepository;
    private final ViceAdminDetailRepository viceAdminDetailRepository;
    private final VillageHeadDetailRepository villageHeadDetailRepository;

    /**총 관리자 전용 즉시 생성*/
    @Transactional
    public void createTreesTransaction(ApprovalTreesTransactionRequestDto approvalTreesTransactionRequestDto) {
        Farmer farmer = farmerRepository.findById(approvalTreesTransactionRequestDto.getFarmerId())
                .orElseThrow(()-> new CustomException(ErrorValue.FARMER_NOT_FOUND.getMessage()));
        if(!farmer.getIsApproved()) throw new CustomException(ErrorValue.FARMER_NOT_FOUND.getMessage());
        TreesTransaction treesTransaction = TreesTransaction.builder()
                .farmer(farmer)
                .species(approvalTreesTransactionRequestDto.getSpecies())
                .receivedDate(approvalTreesTransactionRequestDto.getReceivedDate())
                .quantity(approvalTreesTransactionRequestDto.getQuantity())
                .isApproved(true)
                .build();
        treesTransactionRepository.save(treesTransaction);
    }

    @Transactional
    public ApprovalTreesTransactionRequestDto requestApprovalToCreateTreesTransaction(ApprovalTreesTransactionRequestDto approvalTreesTransactionRequestDto) {
        Farmer farmer = farmerRepository.findById(approvalTreesTransactionRequestDto.getFarmerId())
                .orElseThrow(()-> new CustomException(ErrorValue.FARMER_NOT_FOUND.getMessage()));
        if(!farmer.getIsApproved()) throw new CustomException(ErrorValue.FARMER_NOT_FOUND.getMessage());
        TreesTransaction treesTransaction = TreesTransaction.builder()
                .farmer(farmer)
                .species(approvalTreesTransactionRequestDto.getSpecies())
                .receivedDate(approvalTreesTransactionRequestDto.getReceivedDate())
                .quantity(approvalTreesTransactionRequestDto.getQuantity())
                .build();
        treesTransactionRepository.save(treesTransaction);
        approvalTreesTransactionRequestDto.setId(treesTransaction.getId());
        return approvalTreesTransactionRequestDto;
    }

    @Transactional(readOnly = true)
    public List<TreeTransactionResponseDto> getTreeTransactions(AppUser appUser) {
        Role role = appUser.getRole();

        return switch (role) {
            case ADMIN -> treesTransactionRepository.findByIsApprovedTrueOrderByReceivedDateDesc().stream()
                    .map(TreeTransactionResponseDto::from)
                    .toList();

            case VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER -> {
                ViceAdminDetail detail = viceAdminDetailRepository.findByAppUserId(appUser.getId())
                        .orElseThrow(() -> new CustomException("부 관리자 정보가 존재하지 않습니다."));
                Long areaId = detail.getArea().getId();
                yield treesTransactionRepository.findByIsApprovedTrueAndFarmer_VillageHead_Section_Area_IdOrderByReceivedDateDesc(areaId)
                        .stream().map(TreeTransactionResponseDto::from).toList();
            }

            case VILLAGE_HEAD -> {
                VillageHeadDetail detail = villageHeadDetailRepository.findById(appUser.getId())
                        .orElseThrow(() -> new CustomException("면장 정보가 존재하지 않습니다."));
                Long sectionId = detail.getSection().getId();
                yield treesTransactionRepository.findByIsApprovedTrueAndFarmer_VillageHead_Section_IdOrderByReceivedDateDesc(sectionId)
                        .stream().map(TreeTransactionResponseDto::from).toList();
            }
        };
    }
}
