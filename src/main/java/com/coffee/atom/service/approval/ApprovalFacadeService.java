package com.coffee.atom.service.approval;

import com.coffee.atom.domain.approval.EntityReference;
import com.coffee.atom.domain.approval.EntityType;
import com.coffee.atom.domain.approval.Method;
import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.approval.ApprovalFarmerRequestDto;
import com.coffee.atom.dto.approval.ApprovalTreesTransactionRequestDto;
import com.coffee.atom.dto.approval.ApprovalVillageHeadRequestDto;
import com.coffee.atom.service.AppUserService;
import com.coffee.atom.service.TreesTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalFacadeService {

    private final AppUserService appUserService;
    private final ApprovalService approvalService;
    private final TreesTransactionService treesTransactionService;

    /**
     * 두 서비스를 Transaction으로 묶어 exception 발생 시 전체 프로세스를 rollback
     * */
    @Transactional
    public void processVillageHeadCreation(
            AppUser requester,
            Long approverId,
            ApprovalVillageHeadRequestDto dto
    ) throws JsonProcessingException {
        Long appUserId = appUserService.requestApprovalToCreateVillageHead(requester, dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.VILLAGE_HEAD,
                List.of(
                    new EntityReference(EntityType.APP_USER, appUserId),
                    new EntityReference(EntityType.VILLAGE_HEAD_DETAIL, appUserId)
                )
        );
    }

    @Transactional
    public void processFarmerCreation(
            AppUser requester,
            Long approverId,
            ApprovalFarmerRequestDto dto
    ) throws JsonProcessingException{
        Long appUserId = appUserService.requestApprovalToCreateFarmer(requester, dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.FARMER,
                List.of(
                    new EntityReference(EntityType.FARMER, appUserId)
                )
        );
    }

    @Transactional
    public void processTreesTransactionCreation(
            AppUser requester,
            Long approverId,
            ApprovalTreesTransactionRequestDto dto
    ) throws JsonProcessingException{
        Long appUserId = treesTransactionService.requestApprovalToCreateTreesTransaction(dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.TREES_TRANSACTION,
                List.of(
                    new EntityReference(EntityType.TREES_TRANSACTION, appUserId)
                )
        );
    }
}
