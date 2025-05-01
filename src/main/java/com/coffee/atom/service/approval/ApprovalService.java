package com.coffee.atom.service.approval;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.*;
import com.coffee.atom.domain.approval.*;
import com.coffee.atom.domain.appuser.*;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final RequestedInstanceRepository requestedInstanceRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final ViceAdminDetailRepository viceAdminDetailRepository;
    private final TreesTransactionRepository treesTransactionRepository;
    private final SectionRepository sectionRepository;
    private final FarmerRepository farmerRepository;
    private final VillageHeadDetailRepository villageHeadDetailRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public void requestApproval(AppUser requester,
                                Long approverId,
                                Object requestDto,
                                Method method,
                                ServiceType serviceType,
                                List<EntityReference> affectedEntities) throws JsonProcessingException {
        AppUser approver = appUserRepository.findById(approverId).orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        // 1. 요청 내용을 JSON으로 직렬화
        String requestedJson = objectMapper.writeValueAsString(requestDto);

        // 2. Approval 객체 생성
        Approval approval = Approval.builder()
                .requester(requester)
                .approver(approver)
                .status(Status.PENDING)
                .method(method)
                .serviceType(serviceType)
                .requestedData(requestedJson)
                .build();

        // 3. 영향받는 엔티티들을 RequestedInstance로 매핑
        List<RequestedInstance> instances = affectedEntities.stream()
                .map(ref -> RequestedInstance.builder()
                        .entityType(ref.entityType())
                        .instanceId(ref.instanceId())
                        .approval(approval)
                        .build())
                .toList();

        approval.setRequestedInstance(instances); // 양방향 연결

        // 4. 저장
        approvalRepository.save(approval);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalResponseDto> findApprovals(
        List<Status> statuses,
        List<ServiceType> serviceTypes,
        Pageable pageable,
        AppUser appUser
    ) {
        Specification<Approval> spec = Specification.where(null);

        Role role = appUser.getRole();

        switch (role) {
            case ADMIN -> {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("approver").get("id"), appUser.getId()));
            }
            case VICE_ADMIN_HEAD_OFFICER -> {
                // 나 또는 같은 Area의 농림부 부관리자 요청만
                Long myId = appUser.getId();
                Long areaId = viceAdminDetailRepository.findAreaIdByAppUser_Id(myId)
                        .orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND.getMessage()));
                List<Long> requesterIds = viceAdminDetailRepository.findViceAdminUserIdsByAreaId(areaId);
                spec = spec.and((root, query, cb) -> root.get("requester").get("id").in(requesterIds));
            }
            case VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER ->
                    spec = spec.and((root, query, cb) -> cb.equal(root.get("requester"), appUser));
            default -> throw new CustomException("해당 권한으로 요청 목록을 조회할 수 없습니다.");
        }

        // 공통 필터
        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(statuses));
        }
        if (serviceTypes != null && !serviceTypes.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("serviceType").in(serviceTypes));
        }

        return approvalRepository.findAll(spec, pageable)
                                 .map(ApprovalResponseDto::from);
    }

    @Transactional
    public void processApproval(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
        switch (approval.getMethod()) {
            case CREATE -> handleCreateApproval(approval);
            case UPDATE -> handleUpdateApproval(approval);
            case DELETE -> handleDeleteApproval(approval);
            default -> throw new UnsupportedOperationException("지원하지 않는 승인 방식입니다.");
        }

        approval.setStatus(Status.APPROVED);
        approvalRepository.save(approval);
    }

    // 1. CREATE 처리
    private void handleCreateApproval(Approval approval) {
        for (RequestedInstance instance : approval.getRequestedInstance()) {
            EntityType type = instance.getEntityType();
            Long instanceId = instance.getInstanceId();

            switch (type) {
                case VILLAGE_HEAD_DETAIL -> {
                    VillageHeadDetail entity = villageHeadDetailRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException("해당 VillageHeadDetail이 존재하지 않습니다."));
                    entity.approveInstance();
                }
                case FARMER -> {
                    Farmer entity = farmerRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException("해당 Farmer가 존재하지 않습니다."));
                    entity.approveInstance();
                }
                case SECTION -> {
                    Section entity = sectionRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException("해당 Section이 존재하지 않습니다."));
                    entity.approveInstance();
                }
                case TREES_TRANSACTION -> {
                    TreesTransaction entity = treesTransactionRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException("해당 TreesTransaction이 존재하지 않습니다."));
                    entity.approveInstance();
                }
                case APP_USER -> {
                    AppUser entity = appUserRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException("해당 AppUser가 존재하지 않습니다."));
                    entity.approveInstance();
                }
                case PURCHASE -> {
                    Purchase entity = purchaseRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException("해당 Purchase가 존재하지 않습니다."));
                    entity.approveInstance();
                }
                default -> throw new UnsupportedOperationException("지원되지 않는 엔티티입니다: " + type);
            }
        }
    }

    // 2. UPDATE 처리
    private void handleUpdateApproval(Approval approval) {
        for (RequestedInstance instance : approval.getRequestedInstance()) {
            EntityType type = instance.getEntityType();
            Long id = instance.getInstanceId();
            String requestedData = approval.getRequestedData();

            try {
                JsonNode jsonNode = new ObjectMapper().readTree(requestedData);

                /*if (type == EntityType.FARMER) {
                    Farmer farmer = farmerRepository.findById(id)
                            .orElseThrow(() -> new CustomException(ErrorValue.FARMER_NOT_FOUND.getMessage()));
                    if (jsonNode.has("name")) {
                        farmer.setName(jsonNode.get("name").asText());
                    }
                    if (jsonNode.has("sectionId")) {
                        Section section = sectionRepository.findById(jsonNode.get("sectionId").asLong())
                                .orElseThrow(() -> new CustomException("Section 없음"));
                        farmer.setSection(section);
                    }
                }*/

                // 필요 시 다른 EntityType 처리 가능

            } catch (JsonProcessingException e) {
                throw new CustomException("요청 데이터 파싱 실패");
            }
        }
    }

    // 3. DELETE 처리
    private void handleDeleteApproval(Approval approval) {
        for (RequestedInstance instance : approval.getRequestedInstance()) {
            EntityType type = instance.getEntityType();
            Long id = instance.getInstanceId();

            switch (type) {
                case VILLAGE_HEAD_DETAIL -> villageHeadDetailRepository.deleteById(id);
                case FARMER -> farmerRepository.deleteById(id);
                case SECTION -> sectionRepository.deleteById(id);
                case TREES_TRANSACTION -> treesTransactionRepository.deleteById(id);
                case APP_USER -> appUserRepository.deleteById(id);
                case PURCHASE -> purchaseRepository.deleteById(id);
                default -> throw new UnsupportedOperationException("삭제 불가 엔티티입니다: " + type);
            }
        }
    }

    @Transactional
    public void rejectApproval(Long approvalId, String rejectedReason) {
        Approval approval = approvalRepository.findById(approvalId).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        switch (approval.getMethod()) {
            case CREATE -> rejectCreateApproval(approval);
            case UPDATE -> {}
            case DELETE -> rejectDeleteApproval(approval);
            default -> throw new UnsupportedOperationException("지원하지 않는 승인 방식입니다.");
        }

        approval.setStatus(Status.REJECTED);
        approval.setRejectedReason(rejectedReason);
        approvalRepository.save(approval);
    }

        private void rejectCreateApproval(Approval approval) {
        for (RequestedInstance instance : approval.getRequestedInstance()) {
            EntityType type = instance.getEntityType();
            Long id = instance.getInstanceId();

            switch (type) {
                case VILLAGE_HEAD_DETAIL -> villageHeadDetailRepository.deleteById(id);
                case FARMER -> farmerRepository.deleteById(id);
                case SECTION -> sectionRepository.deleteById(id);
                case TREES_TRANSACTION -> treesTransactionRepository.deleteById(id);
                case APP_USER -> appUserRepository.deleteById(id);
                case PURCHASE -> purchaseRepository.deleteById(id);
                default -> throw new UnsupportedOperationException("삭제 불가 엔티티입니다: " + type);
            }
        }
    }

    private void rejectDeleteApproval(Approval approval) {
        for (RequestedInstance instance : approval.getRequestedInstance()) {
            EntityType type = instance.getEntityType();
            Long id = instance.getInstanceId();

            switch (type) {
                case VILLAGE_HEAD_DETAIL -> villageHeadDetailRepository.findById(id)
                    .ifPresent(VillageHeadDetail::approveInstance);
                case FARMER -> farmerRepository.findById(id)
                    .ifPresent(Farmer::approveInstance);
                case SECTION -> sectionRepository.findById(id)
                    .ifPresent(Section::approveInstance);
                case TREES_TRANSACTION -> treesTransactionRepository.findById(id)
                    .ifPresent(TreesTransaction::approveInstance);
                case APP_USER -> appUserRepository.findById(id)
                    .ifPresent(AppUser::approveInstance);
                case PURCHASE -> purchaseRepository.findById(id)
                        .ifPresent(Purchase::approveInstance);
                default -> throw new UnsupportedOperationException("복구 불가 엔티티입니다: " + type);
            }
        }
    }

    @Transactional(readOnly = true)
    public ApprovalDetailResponse getApprovalDetail(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
        ServiceType type = approval.getServiceType();
        String json = approval.getRequestedData();
        Status status = approval.getStatus();
        String rejectedReason = approval.getRejectedReason();

        try {
            return switch (type) {
                case FARMER -> fromJson(json, FarmerDetailResponseDto.class, type, status, rejectedReason);
                case SECTION -> fromJson(json, SectionDetailResponseDto.class, type, status, rejectedReason);
                case PURCHASE -> fromJson(json, PurchaseDetailResponseDto.class, type, status, rejectedReason);
                case VILLAGE_HEAD -> fromJson(json, VillageHeadDetailResponseDto.class, type, status, rejectedReason);
                case TREES_TRANSACTION -> fromJson(json, TreesTransactionDetailResponseDto.class, type, status, rejectedReason);
            };
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 파싱 실패! 원본: {}", json);
            throw new CustomException("요청 데이터를 파싱할 수 없습니다.");
        }
    }

    private <T extends ApprovalDetailResponse> T fromJson(String json, Class<T> clazz, ServiceType type, Status status, String rejectedReason) throws JsonProcessingException {
        T dto = new ObjectMapper().readValue(json, clazz);
        dto.setStatus(status);
        dto.setServiceType(type);
        dto.setRejectedReason(rejectedReason);
        return dto;
    }
}