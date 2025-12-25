package com.coffee.atom.service.approval;

import com.coffee.atom.domain.approval.ApprovalRepository;
import com.coffee.atom.domain.approval.RequestedInstanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ApprovalProcessingServiceTest {

    @Test
    void canConstruct() {
        ApprovalRepository approvalRepository = mock(ApprovalRepository.class);
        RequestedInstanceRepository requestedInstanceRepository = mock(RequestedInstanceRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ApprovalProcessingService service = new ApprovalProcessingService(approvalRepository, requestedInstanceRepository, objectMapper);

        assertThat(service).isNotNull();
    }
}


