package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.ApprovalSectionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final AreaRepository areaRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public Long requestApprovalToCreateSection(AppUser requester, ApprovalSectionRequestDto approvalSectionRequestDto) {
        if(requester.getRole().equals(Role.VILLAGE_HEAD)) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        Area area = areaRepository.findById(approvalSectionRequestDto.getAreaId()).orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND.getMessage()));

        Section section = Section.builder()
                .sectionName(approvalSectionRequestDto.getSectionName())
                .latitude(approvalSectionRequestDto.getLatitude())
                .longitude(approvalSectionRequestDto.getLongitude())
                .area(area)
                .build();
        sectionRepository.save(section);
        return section.getId();
    }
}
