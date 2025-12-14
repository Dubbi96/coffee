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
import com.coffee.atom.dto.area.SectionDto;
import com.coffee.atom.dto.section.SectionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final AreaRepository areaRepository;
    private final SectionRepository sectionRepository;

    @Transactional
    public ApprovalSectionRequestDto requestApprovalToCreateSection(AppUser requester, ApprovalSectionRequestDto approvalSectionRequestDto) {
        if(requester.getRole().equals(Role.VILLAGE_HEAD)) throw new CustomException(ErrorValue.UNAUTHORIZED);
        
        Area area;
        
        // ADMIN인 경우: requestDTO의 areaId를 기준으로 Area 조회
        if (requester.getRole() == Role.ADMIN) {
            if (approvalSectionRequestDto.getAreaId() == null) {
                throw new CustomException(ErrorValue.AREA_NOT_FOUND);
            }
            area = areaRepository.findById(approvalSectionRequestDto.getAreaId())
                    .orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND));
        } 
        // 부관리자인 경우: requester의 Area 사용
        else if (requester.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || 
                 requester.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            area = requester.getArea();
            if(area == null) throw new CustomException(ErrorValue.AREA_NOT_FOUND);
        } 
        else {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        Section section = Section.builder()
                .sectionName(approvalSectionRequestDto.getSectionName())
                .latitude(approvalSectionRequestDto.getLatitude())
                .longitude(approvalSectionRequestDto.getLongitude())
                .area(area)
                .build();
        sectionRepository.save(section);
        approvalSectionRequestDto.setId(section.getId());
        return approvalSectionRequestDto;
    }

    @Transactional
    public void createSection(AppUser requester, SectionRequestDto sectionRequestDto) {
        // 총 관리자만 Section 생성 가능
        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        Area area = areaRepository.findById(sectionRequestDto.getAreaId()).orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND));
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
        sectionRepository.findById(sectionId).orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND));
        sectionRepository.deleteById(sectionId);
    }

    @Transactional(readOnly = true)
    public SectionDto getSectionById(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));

        return SectionDto.builder()
                .id(section.getId())
                .sectionName(section.getSectionName())
                .latitude(section.getLatitude())
                .longitude(section.getLongitude())
                .build();
    }

}
