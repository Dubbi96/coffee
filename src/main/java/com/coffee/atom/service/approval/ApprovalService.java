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
import com.coffee.atom.util.GCSUtil;
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
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final SectionRepository sectionRepository;
    private final FarmerRepository farmerRepository;
    private final PurchaseRepository purchaseRepository;
    private final GCSUtil gcsUtil;

    @Transactional
    public void requestApproval(AppUser requester,
                                Long approverId,
                                Object requestDto,
                                Method method,
                                ServiceType serviceType,
                                List<EntityReference> affectedEntities) throws JsonProcessingException {
        AppUser approver = appUserRepository.findById(approverId).orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));
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
        List<RequestedInstance> instances = new ArrayList<>();
        for (EntityReference ref : affectedEntities) {
            RequestedInstance instance = RequestedInstance.builder()
                    .entityType(ref.entityType())
                    .instanceId(ref.instanceId())
                    .approval(approval)
                    .build();
            instances.add(instance);
        }

        approval.setRequestedInstance(instances); // 양방향 연결

        // 4. 저장
        approvalRepository.save(approval);
        
        // 5. ADMIN이 요청한 경우 자동 승인 처리
        if (requester.getRole() == Role.ADMIN) {
            log.info("ADMIN user {} created approval {}, auto-approving...", requester.getId(), approval.getId());
            try {
                processApproval(approval.getId(), requester);
                log.info("Auto-approval completed for approval {} by ADMIN {}", approval.getId(), requester.getId());
            } catch (Exception e) {
                log.error("Failed to auto-approve approval {} by ADMIN {}: {}", approval.getId(), requester.getId(), e.getMessage(), e);
                throw e;
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<ApprovalResponseDto> findApprovals(
        List<Status> statuses,
        List<ServiceType> serviceTypes,
        Pageable pageable,
        AppUser appUser
    ) {
        // 면장은 Approval을 조회할 수 없음
        if (appUser.getRole() == Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        Specification<Approval> spec = Specification.where(null);

        Role role = appUser.getRole();

        switch (role) {
            case ADMIN -> {
                // ADMIN은 본인이 승인자로 지정된 요청 중, ADMIN, VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER가 요청한 것만 조회
                // VILLAGE_HEAD가 요청한 것은 제외
                List<Role> allowedRequesterRoles = List.of(
                    Role.ADMIN, 
                    Role.VICE_ADMIN_HEAD_OFFICER, 
                    Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER
                );
                Long adminId = appUser.getId();
                spec = spec.and((root, query, cb) -> 
                    cb.and(
                        // approver가 NULL이거나 현재 ADMIN과 같은 경우
                        cb.or(
                            cb.isNull(root.get("approver")),
                            cb.equal(root.get("approver").get("id"), adminId)
                        ),
                        // requester가 허용된 역할인 경우
                        root.get("requester").get("role").in(allowedRequesterRoles)
                    )
                );
                log.debug("ADMIN approval query - adminId: {}, allowedRequesterRoles: {}", adminId, allowedRequesterRoles);
                
                // 디버깅: 실제 쿼리 결과 확인
                List<Approval> allApprovals = approvalRepository.findAll(spec);
                log.debug("ADMIN query found {} total approvals before pagination", allApprovals.size());
                for (Approval a : allApprovals) {
                    log.debug("Approval id={}, approver={}, requester={}, requesterRole={}, status={}", 
                        a.getId(), 
                        a.getApprover() != null ? a.getApprover().getId() : "NULL",
                        a.getRequester().getId(),
                        a.getRequester().getRole(),
                        a.getStatus());
                }
            }
            case VICE_ADMIN_HEAD_OFFICER -> {
                // 나 또는 같은 Area의 농림부 부관리자 요청만
                if (appUser.getArea() == null) {
                    throw new CustomException(ErrorValue.AREA_NOT_FOUND);
                }
                Long areaId = appUser.getArea().getId();
                List<Role> viceAdminRoles = List.of(Role.VICE_ADMIN_HEAD_OFFICER, Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER);
                List<Long> requesterIds = appUserRepository.findViceAdminUserIdsByAreaId(areaId, viceAdminRoles);
                spec = spec.and((root, query, cb) -> root.get("requester").get("id").in(requesterIds));
            }
            case VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER ->
                    spec = spec.and((root, query, cb) -> cb.equal(root.get("requester"), appUser));
            default -> throw new CustomException(ErrorValue.ROLE_NOT_ALLOWED_APPROVAL_LIST);
        }

        // 공통 필터
        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(statuses));
        }
        if (serviceTypes != null && !serviceTypes.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("serviceType").in(serviceTypes));
        }

        // 페이지네이션 처리: 요청된 페이지가 총 페이지 수를 초과하면 첫 페이지로 조정
        int requestedPage = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "id");
        
        // 먼저 전체 개수를 확인하여 총 페이지 수 계산
        long totalCount = approvalRepository.count(spec);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        
        // 요청된 페이지가 총 페이지 수를 초과하면 첫 페이지로 조정
        int actualPage = requestedPage;
        if (totalPages > 0 && requestedPage >= totalPages) {
            actualPage = 0;
            log.warn("Requested page {} exceeds total pages {}. Adjusting to page 0.", requestedPage, totalPages);
        }
        
        Pageable adjustedPageable = PageRequest.of(actualPage, pageSize, sort);
        
        Page<Approval> approvals = approvalRepository.findAll(spec, adjustedPageable);
        log.info("Found {} approvals for user {} with role {}, requestedPage: {}, actualPage: {}, size: {}, totalPages: {}", 
            approvals.getTotalElements(), appUser.getId(), appUser.getRole(), 
            requestedPage, actualPage, pageSize, approvals.getTotalPages());
        
        return approvals.map(ApprovalResponseDto::from);
    }

    @Transactional
    public void processApproval(Long approvalId, AppUser approver) {
        // 면장은 Approval을 승인할 수 없음
        if (approver.getRole() == Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        Approval approval = approvalRepository.findById(approvalId).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));
        
        // 승인자가 맞는지 확인 (ADMIN이 자기 자신을 승인하는 경우는 허용)
        if (approval.getApprover() != null && !approval.getApprover().getId().equals(approver.getId())) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }
        // ADMIN이 요청한 경우, approver가 null이거나 자기 자신이어야 함
        if (approval.getApprover() == null && approver.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

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
                case APP_USER -> {
                    AppUser entity = appUserRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException(ErrorValue.APP_USER_NOT_FOUND));
                    entity.approveInstance();
                }
                case FARMER -> {
                    Farmer entity = farmerRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException(ErrorValue.FARMER_NOT_FOUND));
                    entity.approveInstance();
                }
                case SECTION -> {
                    Section entity = sectionRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND));
                    entity.approveInstance();
                }
                case PURCHASE -> {
                    Purchase entity = purchaseRepository.findById(instanceId)
                            .orElseThrow(() -> new CustomException(ErrorValue.PURCHASE_NOT_FOUND));
                    entity.approveInstance();
                }
                default -> throw new UnsupportedOperationException("지원되지 않는 엔티티입니다: " + type);
            }
        }
    }

    // 2. UPDATE 처리
    private void handleUpdateApproval(Approval approval) {
        AppUser requester = approval.getRequester();
        
        for (RequestedInstance instance : approval.getRequestedInstance()) {
            EntityType type = instance.getEntityType();
            Long id = instance.getInstanceId();
            String requestedData = approval.getRequestedData();

            try {
                JsonNode jsonNode = new ObjectMapper().readTree(requestedData);

                if (type == EntityType.APP_USER) {
                    AppUser appUser = appUserRepository.findById(id)
                            .orElseThrow(() -> new CustomException(ErrorValue.APP_USER_NOT_FOUND));

                    if (appUser.getRole() == Role.VILLAGE_HEAD) {
                        // 계좌정보 및 은행명 업데이트
                        if (jsonNode.has("accountInfo") && isNonEmptyText(jsonNode.get("accountInfo"))) {
                            appUser.updateAccountInfo(jsonNode.get("accountInfo").asText());
                        }

                        if (jsonNode.has("bankName") && isNonEmptyText(jsonNode.get("bankName"))) {
                            appUser.updateBankName(jsonNode.get("bankName").asText());
                        }

                        // section 변경
                        if (jsonNode.has("sectionId") && !jsonNode.get("sectionId").isNull()) {
                            Section section = sectionRepository.findById(jsonNode.get("sectionId").asLong())
                                    .orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND));
                                if (!section.getIsApproved()) {
                                    throw new CustomException(ErrorValue.SECTION_NOT_APPROVED);
                                }
                            appUser.updateSection(section);
                        }

                        // 식별 URL들 (옵셔널) - 파일 URL 변경 시 이전 파일 자동 삭제
                        if (jsonNode.has("identificationPhotoUrl") && isNonEmptyText(jsonNode.get("identificationPhotoUrl"))) {
                            String newUrl = jsonNode.get("identificationPhotoUrl").asText();
                            String existingUrl = appUser.getIdentificationPhotoUrl();
                            gcsUtil.updateFileUrlIfChanged(existingUrl, newUrl, requester);
                            appUser.updateIdentificationPhotoUrl(newUrl);
                        }
                        if (jsonNode.has("contractFileUrl") && isNonEmptyText(jsonNode.get("contractFileUrl"))) {
                            String newUrl = jsonNode.get("contractFileUrl").asText();
                            String existingUrl = appUser.getContractUrl();
                            gcsUtil.updateFileUrlIfChanged(existingUrl, newUrl, requester);
                            appUser.updateContractUrl(newUrl);
                        }
                        if (jsonNode.has("bankbookPhotoUrl") && isNonEmptyText(jsonNode.get("bankbookPhotoUrl"))) {
                            String newUrl = jsonNode.get("bankbookPhotoUrl").asText();
                            String existingUrl = appUser.getBankbookUrl();
                            gcsUtil.updateFileUrlIfChanged(existingUrl, newUrl, requester);
                            appUser.updateBankbookUrl(newUrl);
                        }
                    }

                    appUserRepository.save(appUser);
                }

                if (type == EntityType.FARMER) {
                    Farmer farmer = farmerRepository.findById(id)
                            .orElseThrow(() -> new CustomException(ErrorValue.FARMER_NOT_FOUND));

                    // 이름 업데이트
                    if (jsonNode.has("name")) {
                        farmer.updateName(jsonNode.get("name").asText());
                    }

                    // 식별 이미지 URL 업데이트 (Optional) - 파일 URL 변경 시 이전 파일 자동 삭제
                    if (jsonNode.has("identificationPhotoUrl")) {
                        String newUrl = jsonNode.get("identificationPhotoUrl").asText();
                        String existingUrl = farmer.getIdentificationPhotoUrl();
                        // 파일 URL 변경 확인 및 이전 파일 삭제
                        gcsUtil.updateFileUrlIfChanged(existingUrl, newUrl, requester);
                        farmer.updateIdentificationPhotoUrl(newUrl);
                    }

                    // 소속 면장 변경
                    if (jsonNode.has("villageHeadId")) {
                        Long villageHeadId = jsonNode.get("villageHeadId").asLong();
                        AppUser villageHead = appUserRepository.findById(villageHeadId)
                                .orElseThrow(() -> new CustomException(ErrorValue.VILLAGE_HEAD_NOT_FOUND));
                        if (villageHead.getRole() != Role.VILLAGE_HEAD || 
                            villageHead.getIsApproved() == null || !villageHead.getIsApproved()) {
                            throw new CustomException(ErrorValue.VILLAGE_HEAD_NOT_APPROVED);
                        }
                        farmer.updateVillageHead(villageHead);
                    }

                    farmerRepository.save(farmer);
                }

                if (type == EntityType.PURCHASE) {
                    Purchase purchase = purchaseRepository.findById(id)
                            .orElseThrow(() -> new CustomException(ErrorValue.PURCHASE_NOT_FOUND));

                    // 면장 변경
                    if (jsonNode.has("villageHeadId")) {
                        Long villageHeadId = jsonNode.get("villageHeadId").asLong();
                        AppUser villageHead = appUserRepository.findById(villageHeadId)
                                .orElseThrow(() -> new CustomException(ErrorValue.VILLAGE_HEAD_NOT_FOUND));
                        if (villageHead.getRole() != Role.VILLAGE_HEAD || 
                            villageHead.getIsApproved() == null || !villageHead.getIsApproved()) {
                            throw new CustomException(ErrorValue.VILLAGE_HEAD_NOT_APPROVED);
                        }
                        purchase.updateVillageHead(villageHead);
                    }

                    // 구매일자 업데이트
                    if (jsonNode.has("purchaseDate")) {
                        purchase.updatePurchaseDate(LocalDate.parse(jsonNode.get("purchaseDate").asText()));
                    }

                    // 수량 업데이트
                    if (jsonNode.has("quantity")) {
                        purchase.updateQuantity(jsonNode.get("quantity").asLong());
                    }

                    // 단가 업데이트
                    if (jsonNode.has("unitPrice")) {
                        purchase.updateUnitPrice(jsonNode.get("unitPrice").asLong());
                    }

                    // 총액 업데이트
                    if (jsonNode.has("totalPrice")) {
                        purchase.updateTotalPrice(jsonNode.get("totalPrice").asLong());
                    }

                    // 차감액 업데이트
                    if (jsonNode.has("deduction")) {
                        purchase.updateDeduction(jsonNode.get("deduction").asLong());
                    }

                    // 지급액 업데이트
                    if (jsonNode.has("paymentAmount")) {
                        purchase.updatePaymentAmount(jsonNode.get("paymentAmount").asLong());
                    }

                    // 비고 업데이트
                    if (jsonNode.has("remarks")) {
                        purchase.updateRemarks(jsonNode.get("remarks").asText());
                    }

                    purchaseRepository.save(purchase);
                }

            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
            }
        }
    }

    private boolean isNonEmptyText(JsonNode node) {
        return node != null && !node.isNull() && node.isTextual() && !node.asText().isBlank();
    }

    // 3. DELETE 처리
    private void handleDeleteApproval(Approval approval) {
        AppUser requester = approval.getRequester();
        
        for (RequestedInstance instance : approval.getRequestedInstance()) {
            EntityType type = instance.getEntityType();
            Long id = instance.getInstanceId();

            switch (type) {
                case APP_USER -> {
                    AppUser appUser = appUserRepository.findById(id)
                            .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));
                    
                    // GCS 파일 삭제
                    List<String> fileUrlsToDelete = new ArrayList<>();
                    if (appUser.getRole() == Role.VILLAGE_HEAD) {
                        // 면장의 경우: identificationPhotoUrl, contractUrl, bankbookUrl 삭제
                        if (StringUtils.hasText(appUser.getIdentificationPhotoUrl())) {
                            fileUrlsToDelete.add(appUser.getIdentificationPhotoUrl());
                        }
                        if (StringUtils.hasText(appUser.getContractUrl())) {
                            fileUrlsToDelete.add(appUser.getContractUrl());
                        }
                        if (StringUtils.hasText(appUser.getBankbookUrl())) {
                            fileUrlsToDelete.add(appUser.getBankbookUrl());
                        }
                    } else if (appUser.getRole() == Role.VICE_ADMIN_HEAD_OFFICER ||
                               appUser.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
                        // 부관리자의 경우: idCardUrl 삭제
                        if (StringUtils.hasText(appUser.getIdCardUrl())) {
                            fileUrlsToDelete.add(appUser.getIdCardUrl());
                        }
                    }
                    
                    // GCS에서 파일 삭제
                    if (!fileUrlsToDelete.isEmpty()) {
                        gcsUtil.deleteFileFromGCS(fileUrlsToDelete, requester);
                    }
                    
                    appUserRepository.deleteById(id);
                }
                case FARMER -> {
                    Farmer farmer = farmerRepository.findById(id)
                            .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));
                    
                    // GCS 파일 삭제: identificationPhotoUrl
                    if (StringUtils.hasText(farmer.getIdentificationPhotoUrl())) {
                        gcsUtil.deleteFileFromGCS(farmer.getIdentificationPhotoUrl(), requester);
                    }
                    
                    farmerRepository.deleteById(id);
                }
                case SECTION -> {
                    sectionRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));
                    sectionRepository.deleteById(id);
                }
                case PURCHASE -> {
                    purchaseRepository.findById(id).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));
                    purchaseRepository.deleteById(id);
                }
                default -> throw new UnsupportedOperationException("삭제 불가 엔티티입니다: " + type);
            }
        }
    }

    @Transactional
    public void rejectApproval(Long approvalId, String rejectedReason, AppUser approver) {
        // 면장은 Approval을 거절할 수 없음
        if (approver.getRole() == Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        Approval approval = approvalRepository.findById(approvalId).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));

        // 승인자가 맞는지 확인
        if (!approval.getApprover().getId().equals(approver.getId())) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

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
            case APP_USER -> 2;
            case SECTION -> 3;
            case PURCHASE -> 4;
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
                case APP_USER -> appUserRepository.deleteById(id);
                case FARMER -> farmerRepository.deleteById(id);
                case SECTION -> sectionRepository.deleteById(id);
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
                case APP_USER -> appUserRepository.findById(id)
                    .ifPresent(AppUser::approveInstance);
                case FARMER -> farmerRepository.findById(id)
                    .ifPresent(Farmer::approveInstance);
                case SECTION -> sectionRepository.findById(id)
                    .ifPresent(Section::approveInstance);
                case PURCHASE -> purchaseRepository.findById(id)
                        .ifPresent(Purchase::approveInstance);
                default -> throw new UnsupportedOperationException("복구 불가 엔티티입니다: " + type);
            }
        }
    }

    @Transactional(readOnly = true)
    public ApprovalDetailResponse getApprovalDetail(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId).orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));
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
            };
            if (dto instanceof VillageHeadDetailResponseDto v) {
                enrichVillageHeadDetail(v);
            }
            if (dto instanceof FarmerDetailResponseDto v) {
                enrichFarmerDetail(v);
            }
            return dto;
        } catch (JsonProcessingException e) {
            log.error("❌ JSON 파싱 실패! 원본: {}", json);
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
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

        appUserRepository.findById(villageHeadId).ifPresent(villageHead -> {
            if (villageHead.getSection() != null) {
                Section section = villageHead.getSection();
                dto.setSectionId(section.getId());
                dto.setSectionName(section.getSectionName());

                if (section.getArea() != null) {
                    dto.setAreaId(section.getArea().getId());
                    dto.setAreaName(section.getArea().getAreaName());
                }
            }
        });
    }

    @Transactional
    public void deleteApproval(Long approvalId, AppUser appUser) {
        // 1. Approval 엔티티 조회
        Approval approval = approvalRepository.findById(approvalId)
            .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));

        // 2. 요청자 검증
        if (!approval.getRequester().getId().equals(appUser.getId())) {
            throw new CustomException(ErrorValue.UNAUTHORIZED_SERVICE);
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