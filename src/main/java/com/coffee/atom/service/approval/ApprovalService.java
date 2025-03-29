package com.coffee.atom.service.approval;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.approval.*;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.appuser.ViceAdminDetailRepository;
import com.coffee.atom.dto.approval.ApprovalResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final RequestedInstanceRepository requestedInstanceRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;
    private final ViceAdminDetailRepository viceAdminDetailRepository;

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
                // 전체 접근 (필터 그대로 적용)
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


}