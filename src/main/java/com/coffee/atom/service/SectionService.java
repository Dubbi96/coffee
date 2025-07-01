package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.appuser.ViceAdminDetail;
import com.coffee.atom.domain.appuser.ViceAdminDetailRepository;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.ApprovalSectionRequestDto;
import com.coffee.atom.dto.section.SectionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final AreaRepository areaRepository;
    private final SectionRepository sectionRepository;
    private final ViceAdminDetailRepository viceAdminDetailRepository;

    @Transactional
    public ApprovalSectionRequestDto requestApprovalToCreateSection(AppUser requester, ApprovalSectionRequestDto approvalSectionRequestDto) {
        if(requester.getRole().equals(Role.VILLAGE_HEAD)) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        ViceAdminDetail viceAdminDetail = viceAdminDetailRepository.findById(requester.getId()).orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        if(viceAdminDetail.getArea() == null) throw new CustomException(ErrorValue.AREA_NOT_FOUND.getMessage());

        Section section = Section.builder()
                .sectionName(approvalSectionRequestDto.getSectionName())
                .latitude(approvalSectionRequestDto.getLatitude())
                .longitude(approvalSectionRequestDto.getLongitude())
                .area(viceAdminDetail.getArea())
                .build();
        sectionRepository.save(section);
        approvalSectionRequestDto.setId(section.getId());
        return approvalSectionRequestDto;
    }

    @Transactional
    public void createSection(SectionRequestDto sectionRequestDto) {
        Area area = areaRepository.findById(sectionRequestDto.getAreaId()).orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND.getMessage()));
        Section section = Section.builder()
                .sectionName(sectionRequestDto.getSectionName())
                .latitude(sectionRequestDto.getLatitude())
                .longitude(sectionRequestDto.getLongitude())
                .area(area)
                .isApproved(true)
                .build();
        sectionRepository.save(section);
    }

    @Transactional
    public void deleteSection(Long sectionId) {
        sectionRepository.findById(sectionId).orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND.getMessage()));
        sectionRepository.deleteById(sectionId);
    }
}
