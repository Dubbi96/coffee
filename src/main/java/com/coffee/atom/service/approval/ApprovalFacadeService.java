package com.coffee.atom.service.approval;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.*;
import com.coffee.atom.domain.approval.EntityReference;
import com.coffee.atom.domain.approval.EntityType;
import com.coffee.atom.domain.approval.Method;
import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.VillageHeadDetail;
import com.coffee.atom.domain.appuser.VillageHeadDetailRepository;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.*;
import com.coffee.atom.service.AppUserService;
import com.coffee.atom.service.PurchaseService;
import com.coffee.atom.service.SectionService;
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
    private final PurchaseService purchaseService;
    private final SectionService sectionService;
    private final FarmerRepository farmerRepository;
    private final VillageHeadDetailRepository villageHeadDetailRepository;
    private final SectionRepository sectionRepository;
    private final TreesTransactionRepository treesTransactionRepository;
    private final PurchaseRepository purchaseRepository;

    /**
     * 두 서비스를 Transaction으로 묶어 exception 발생 시 전체 프로세스를 rollback
     * */
    @Transactional
    public void processVillageHeadCreation(
            AppUser requester,
            Long approverId,
            ApprovalVillageHeadRequestDto dto
    ) throws JsonProcessingException {
        dto = appUserService.requestApprovalToCreateVillageHead(requester, dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.VILLAGE_HEAD,
                List.of(
                    new EntityReference(EntityType.APP_USER, dto.getId()),
                    new EntityReference(EntityType.VILLAGE_HEAD_DETAIL, dto.getId())
                )
        );
    }

    @Transactional
    public void processFarmerCreation(
            AppUser requester,
            Long approverId,
            ApprovalFarmerRequestDto dto
    ) throws JsonProcessingException{
        dto = appUserService.requestApprovalToCreateFarmer(requester, dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.FARMER,
                List.of(
                    new EntityReference(EntityType.FARMER, dto.getId())
                )
        );
    }

    @Transactional
    public void processTreesTransactionCreation(
            AppUser requester,
            Long approverId,
            ApprovalTreesTransactionRequestDto dto
    ) throws JsonProcessingException{
        dto = treesTransactionService.requestApprovalToCreateTreesTransaction(dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.TREES_TRANSACTION,
                List.of(
                    new EntityReference(EntityType.TREES_TRANSACTION, dto.getId())
                )
        );
    }

    @Transactional
    public void processPurchaseCreation(
            AppUser requester,
            Long approverId,
            ApprovalPurchaseRequestDto dto
    ) throws JsonProcessingException{
        dto = purchaseService.requestApprovalToCreatePurchase(requester, dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.PURCHASE,
                List.of(
                    new EntityReference(EntityType.PURCHASE, dto.getId())
                )
        );
    }

    @Transactional
    public void processSectionCreation(
            AppUser requester,
            Long approverId,
            ApprovalSectionRequestDto dto
    ) throws JsonProcessingException{
        dto = sectionService.requestApprovalToCreateSection(requester, dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.CREATE,
                ServiceType.SECTION,
                List.of(
                    new EntityReference(EntityType.SECTION, dto.getId())
                )
        );
    }

    @Transactional
    public void processVillageHeadUpdate(
            AppUser requester,
            Long approverId,
            ApprovalVillageHeadRequestDto dto
    ) throws JsonProcessingException{
        dto = appUserService.requestApprovalToUpdateVillageHead(requester, dto);
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.UPDATE,
                ServiceType.VILLAGE_HEAD,
                List.of(
                    new EntityReference(EntityType.VILLAGE_HEAD_DETAIL, dto.getId())
                )
        );
    }

    @Transactional
    public void processFarmerUpdate(
            AppUser requester,
            Long approverId,
            ApprovalFarmerRequestDto dto
    ) throws JsonProcessingException {
        dto = appUserService.requestApprovalToUpdateFarmer(requester, dto); // 수정용 DTO 처리
        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.UPDATE, // 생성과의 차이점
                ServiceType.FARMER,
                List.of(new EntityReference(EntityType.FARMER, dto.getId()))
        );
    }

    @Transactional
    public void processFarmerDelete(
            AppUser requester,
            Long approverId,
            Long farmerId
    ) throws JsonProcessingException {
        // 삭제 대상 농부 존재 여부 확인
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        ApprovalFarmerRequestDto deleteDto = new ApprovalFarmerRequestDto(
            null, // 삭제 요청에는 identificationPhoto 업로드 없음
            farmer.getName(),
            farmer.getVillageHead().getId()
        );
        deleteDto.setId(farmer.getId());
        deleteDto.setIdentificationPhotoUrl(farmer.getIdentificationPhotoUrl());

        // 승인 요청
        approvalService.requestApproval(
                requester,
                approverId,
                deleteDto, // 주의: 삭제 요청이라도 dto 구조는 동일하게 전달
                Method.DELETE,
                ServiceType.FARMER,
                List.of(new EntityReference(EntityType.FARMER, deleteDto.getId()))
        );
    }

    @Transactional
    public void processVillageHeadDelete(
            AppUser requester,
            Long approverId,
            Long villageHeadId
    ) throws JsonProcessingException {
        VillageHeadDetail villageHead = villageHeadDetailRepository.findById(villageHeadId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        // 삭제 요청용 DTO 생성 (파일은 null, URL만 포함)
        ApprovalVillageHeadRequestDto dto = new ApprovalVillageHeadRequestDto(
                villageHead.getId(),
                villageHead.getAppUser().getUserId(),
                villageHead.getAppUser().getPassword(), // 암호화된 값 그대로 사용
                villageHead.getAppUser().getUsername(),
                villageHead.getBankName(),
                villageHead.getAccountInfo(),
                null, // MultipartFile
                null,
                null,
                villageHead.getSection().getId()
        );
        dto.setIdentificationPhotoUrl(villageHead.getIdentificationPhotoUrl());
        dto.setContractFileUrl(villageHead.getContractUrl());
        dto.setBankbookPhotoUrl(villageHead.getBankbookUrl());

        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.DELETE,
                ServiceType.VILLAGE_HEAD,
                List.of(new EntityReference(EntityType.VILLAGE_HEAD_DETAIL, villageHead.getId()))
        );
    }

    @Transactional
    public void processSectionDelete(
            AppUser requester,
            Long approverId,
            Long sectionId
    ) throws JsonProcessingException {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        ApprovalSectionRequestDto dto = new ApprovalSectionRequestDto();
        dto.setId(section.getId());
        dto.setLatitude(section.getLatitude());
        dto.setLongitude(section.getLongitude());
        dto.setSectionName(section.getSectionName());
        dto.setAreaId(section.getArea().getId());

        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.DELETE,
                ServiceType.SECTION,
                List.of(new EntityReference(EntityType.SECTION, dto.getId()))
        );
    }

    @Transactional
    public void processTreesTransactionDelete(
            AppUser requester,
            Long approverId,
            Long treesTransactionId
    ) throws JsonProcessingException {
        TreesTransaction transaction = treesTransactionRepository.findById(treesTransactionId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        ApprovalTreesTransactionRequestDto dto = new ApprovalTreesTransactionRequestDto();
        dto.setId(transaction.getId());
        dto.setSpecies(transaction.getSpecies());
        dto.setFarmerId(transaction.getFarmer().getId());
        dto.setQuantity(transaction.getQuantity());
        dto.setReceivedDate(transaction.getReceivedDate());

        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.DELETE,
                ServiceType.TREES_TRANSACTION,
                List.of(new EntityReference(EntityType.TREES_TRANSACTION, dto.getId()))
        );
    }

    @Transactional
    public void processPurchaseDelete(
            AppUser requester,
            Long approverId,
            Long purchaseId
    ) throws JsonProcessingException {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        ApprovalPurchaseRequestDto dto = new ApprovalPurchaseRequestDto();
        dto.setId(purchase.getId());
        dto.setDeduction(purchase.getDeduction());
        dto.setPaymentAmount(purchase.getPaymentAmount());
        dto.setPurchaseDate(purchase.getPurchaseDate());
        dto.setQuantity(purchase.getQuantity());
        dto.setTotalPrice(purchase.getTotalPrice());
        dto.setUnitPrice(purchase.getUnitPrice());

        approvalService.requestApproval(
                requester,
                approverId,
                dto,
                Method.DELETE,
                ServiceType.PURCHASE,
                List.of(new EntityReference(EntityType.PURCHASE, dto.getId()))
        );
    }

}
