package com.coffee.atom.service.approval;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.*;
import com.coffee.atom.domain.approval.*;
import com.coffee.atom.domain.appuser.*;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
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

        if (!pageable.getSort().isSorted()) {
            pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "createdAt")
            );
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

                if (type == EntityType.VILLAGE_HEAD_DETAIL) {
                    VillageHeadDetail villageHead = villageHeadDetailRepository.findById(id)
                            .orElseThrow(() -> new CustomException("해당 VillageHeadDetail이 존재하지 않습니다."));

                    // 계좌정보 및 은행명 업데이트
                    if (jsonNode.has("accountInfo")) {
                        villageHead.updateAccountInfo(jsonNode.get("accountInfo").asText());
                    }

                    if (jsonNode.has("bankName")) {
                        villageHead.updateBankName(jsonNode.get("bankName").asText());
                    }

                    // section 변경
                    if (jsonNode.has("sectionId")) {
                        Section section = sectionRepository.findById(jsonNode.get("sectionId").asLong())
                                .orElseThrow(() -> new CustomException("Section이 존재하지 않습니다."));
                        if (!section.getIsApproved()) {
                            throw new CustomException("승인되지 않은 Section입니다.");
                        }
                        villageHead.updateSection(section);
                    }

                    // 식별 URL들 (옵셔널)
                    if (jsonNode.has("identificationPhotoUrl")) {
                        villageHead.updateIdentificationPhotoUrl(jsonNode.get("identificationPhotoUrl").asText());
                    }
                    if (jsonNode.has("contractFileUrl")) {
                        villageHead.updateContractUrl(jsonNode.get("contractFileUrl").asText());
                    }
                    if (jsonNode.has("bankbookPhotoUrl")) {
                        villageHead.updateBankbookUrl(jsonNode.get("bankbookPhotoUrl").asText());
                    }

                    villageHeadDetailRepository.save(villageHead);
                }

                if (type == EntityType.FARMER) {
                    Farmer farmer = farmerRepository.findById(id)
                            .orElseThrow(() -> new CustomException("해당 Farmer가 존재하지 않습니다."));

                    // 이름 업데이트
                    if (jsonNode.has("name")) {
                        farmer.updateName(jsonNode.get("name").asText());
                    }

                    // 식별 이미지 URL 업데이트 (Optional)
                    if (jsonNode.has("identificationPhotoUrl")) {
                        farmer.updateIdentificationPhotoUrl(jsonNode.get("identificationPhotoUrl").asText());
                    }

                    // 소속 면장 변경
                    if (jsonNode.has("villageHeadId")) {
                        Long villageHeadId = jsonNode.get("villageHeadId").asLong();
                        VillageHeadDetail villageHeadDetail = villageHeadDetailRepository.findById(villageHeadId)
                                .orElseThrow(() -> new CustomException("면장이 존재하지 않습니다."));
                        if (!villageHeadDetail.getIsApproved()) {
                            throw new CustomException("승인되지 않은 면장입니다.");
                        }
                        farmer.updateVillageHead(villageHeadDetail);
                    }

                    farmerRepository.save(farmer);
                }


                // (추후 FARMER 등 다른 EntityType도 추가 가능)

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
                case VILLAGE_HEAD_DETAIL -> {
                    villageHeadDetailRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
                    villageHeadDetailRepository.deleteById(id);
                }
                case FARMER ->{
                    farmerRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
                    farmerRepository.deleteById(id);
                }
                case SECTION ->{
                    sectionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
                    sectionRepository.deleteById(id);
                }
                case TREES_TRANSACTION ->{
                    treesTransactionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
                    treesTransactionRepository.deleteById(id);
                }
                case APP_USER ->{
                    appUserRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
                    appUserRepository.deleteById(id);
                }
                case PURCHASE -> {
                    purchaseRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));
                    purchaseRepository.deleteById(id);
                }
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

    private int deletePriority(EntityType type) {
        return switch (type) {
            case FARMER -> 1;
            case VILLAGE_HEAD_DETAIL -> 2;
            case SECTION -> 3;
            case PURCHASE -> 4;
            case TREES_TRANSACTION -> 5;
            case APP_USER -> 6;
            default -> 99;
        };
    }



    private void rejectCreateApproval(Approval approval) {
    // 1. 삭제 우선순위로 정렬
        List<RequestedInstance> sortedInstances = approval.getRequestedInstance().stream()
            .sorted(Comparator.comparingInt(instance -> deletePriority(instance.getEntityType())))
            .toList();

        // 2. 정렬된 순서대로 삭제
        for (RequestedInstance instance : sortedInstances) {
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
        Method method = approval.getMethod();
        Long requesterId = approval.getRequester().getId();
        String requesterName = approval.getRequester().getUsername();
        LocalDateTime createdAt = approval.getCreatedAt();
        try {
            ApprovalDetailResponse dto =  switch (type) {
                case FARMER -> fromJson(json, FarmerDetailResponseDto.class, type, status, rejectedReason, method, requesterId, requesterName, createdAt);
                case SECTION -> fromJson(json, SectionDetailResponseDto.class, type, status, rejectedReason, method, requesterId, requesterName, createdAt);
                case PURCHASE -> fromJson(json, PurchaseDetailResponseDto.class, type, status, rejectedReason, method, requesterId, requesterName, createdAt);
                case VILLAGE_HEAD -> fromJson(json, VillageHeadDetailResponseDto.class, type, status, rejectedReason, method, requesterId, requesterName, createdAt);
                case TREES_TRANSACTION -> fromJson(json, TreesTransactionDetailResponseDto.class, type, status, rejectedReason, method, requesterId, requesterName, createdAt);
            };
            if (dto instanceof VillageHeadDetailResponseDto v) {
                enrichVillageHeadDetail(v);
            }
            if (dto instanceof FarmerDetailResponseDto v) {
                enrichFarmerDetail(v);
            }
            if (dto instanceof TreesTransactionDetailResponseDto v) {
                enrichTreeTransactionDetail(v);
            }
            return dto;
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 파싱 실패! 원본: {}", json);
            throw new CustomException("요청 데이터를 파싱할 수 없습니다.");
        }
    }

    private void enrichTreeTransactionDetail(TreesTransactionDetailResponseDto dto) {
        Long farmerId = dto.getFarmerId();
        if (farmerId == null) return;

        farmerRepository.findById(farmerId).ifPresent(farmer -> {
            dto.setFarmerName(farmer.getName());

            VillageHeadDetail villageHead = farmer.getVillageHead();
            if (villageHead != null) {
                Section section = villageHead.getSection();
                if (section != null) {
                    dto.setSectionName(section.getSectionName());

                    Area area = section.getArea();
                    if (area != null) {
                        dto.setAreaName(area.getAreaName());
                    }
                }
            }
        });
    }

    private void enrichVillageHeadDetail(VillageHeadDetailResponseDto dto) {
        Long sectionId = dto.getSectionId();
        if (sectionId == null) return;

        sectionRepository.findById(sectionId).ifPresent(section -> {
            dto.setSectionName(section.getSectionName());

            if (section.getArea() != null) {
                dto.setAreaId(section.getArea().getId());
                dto.setAreaName(section.getArea().getAreaName());
            }
        });
    }

    private void enrichFarmerDetail(FarmerDetailResponseDto dto) {
        Long villageHeadId = dto.getVillageHeadId();
        if (villageHeadId == null) return;

        villageHeadDetailRepository.findById(villageHeadId).ifPresent(villageHead -> {
            Section section = villageHead.getSection();
            dto.setSectionId(section.getId());
            dto.setSectionName(section.getSectionName());

            if (section.getArea() != null) {
                dto.setAreaId(section.getArea().getId());
                dto.setAreaName(section.getArea().getAreaName());
            }
        });
    }

    @Transactional
    public void deleteApproval(Long approvalId, AppUser appUser) {
        // 1. Approval 엔티티 조회
        Approval approval = approvalRepository.findById(approvalId)
            .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        // 2. 요청자 검증
        if (!approval.getRequester().getId().equals(appUser.getId())) {
            throw new CustomException(ErrorValue.UNAUTHORIZED_SERVICE.getMessage());
        }

        // 3. 삭제
        approvalRepository.delete(approval);
    }

    private <T extends ApprovalDetailResponse> T fromJson(String json, Class<T> clazz, ServiceType type, Status status, String rejectedReason, Method method, Long requesterId,String requesterName, LocalDateTime createdAt) throws JsonProcessingException {
        T dto = new ObjectMapper().readValue(json, clazz);
        dto.setStatus(status);
        dto.setServiceType(type);
        dto.setRejectedReason(rejectedReason);
        dto.setMethod(method);
        dto.setRequesterId(requesterId);
        dto.setRequesterName(requesterName);
        dto.setCreatedAt(createdAt);
        return dto;
    }
}