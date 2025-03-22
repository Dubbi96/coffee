package com.coffee.atom.service.approval;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.approval.*;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
}