package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.TreesTransaction;
import com.coffee.atom.domain.TreesTransactionRepository;
import com.coffee.atom.dto.approval.ApprovalTreesTransactionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TreesTransactionService {

    private final TreesTransactionRepository treesTransactionRepository;
    private final FarmerRepository farmerRepository;

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
}
