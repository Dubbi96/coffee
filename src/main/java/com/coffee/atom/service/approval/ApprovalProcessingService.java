package com.coffee.atom.service.approval;

import com.coffee.atom.domain.approval.ApprovalRepository;
import com.coffee.atom.domain.approval.RequestedInstanceRepository;
import com.coffee.atom.domain.appuser.VillageHeadDetailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApprovalProcessingService {

    private final ApprovalRepository approvalRepository;
    private final RequestedInstanceRepository requestedInstanceRepository;
    private final VillageHeadDetailRepository villageHeadDetailRepository;
    private final ObjectMapper objectMapper;


}
