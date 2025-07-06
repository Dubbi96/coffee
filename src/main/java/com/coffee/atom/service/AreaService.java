package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.*;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.dto.area.AreaDto;
import com.coffee.atom.dto.area.AreaRequestDto;
import com.coffee.atom.dto.area.AreaResponseDto;
import com.coffee.atom.dto.area.AreaSectionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class AreaService {
    private final AreaRepository areaRepository;
    private final ViceAdminDetailRepository viceAdminDetailRepository;

    @Transactional
    public void saveArea(AppUser appUser, AreaRequestDto areaRequestDto) {
        if(!appUser.getRole().equals(Role.ADMIN)) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        Area newArea = Area.builder()
                .areaName(areaRequestDto.getAreaName())
                .longitude(areaRequestDto.getLongitude())
                .latitude(areaRequestDto.getLatitude())
                .build();
        areaRepository.save(newArea);
    }

    @Transactional(readOnly = true)
    public List<AreaSectionResponseDto> getAreasWithSections() {
        return areaRepository.findAreasWithSections().stream()
                .sorted(Comparator.comparing(Area::getAreaName, String.CASE_INSENSITIVE_ORDER))
                .map(area -> AreaSectionResponseDto.builder()
                        .id(area.getId())
                        .areaName(area.getAreaName())
                        .latitude(area.getLatitude())
                        .longitude(area.getLongitude())
                        .sections(
                                area.getSections().stream()
                                        .filter(section -> Boolean.TRUE.equals(section.getIsApproved()))
                                        .sorted(Comparator.comparing(Section::getSectionName, String.CASE_INSENSITIVE_ORDER))
                                        .map(AreaSectionResponseDto.Sections::from)
                                        .toList()
                        )
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AreaSectionResponseDto> getAreaWithSections(Long areaId) {
        return areaRepository.findAreaWithSections(areaId).stream()
                .map(area -> AreaSectionResponseDto.builder()
                        .id(area.getId())
                        .areaName(area.getAreaName())
                        .latitude(area.getLatitude())
                        .longitude(area.getLongitude())
                        .sections(
                                area.getSections().stream()
                                        .filter(section -> Boolean.TRUE.equals(section.getIsApproved()))
                                        .sorted(Comparator.comparing(Section::getSectionName, String.CASE_INSENSITIVE_ORDER))
                                        .map(AreaSectionResponseDto.Sections::from)
                                        .toList()
                        )
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AreaResponseDto> getArea(AppUser appUser) {
        return areaRepository.findAll().stream()
                .sorted(Comparator.comparing(Area::getAreaName, String.CASE_INSENSITIVE_ORDER))
                .map(area -> AreaResponseDto.builder()
                        .id(area.getId())
                        .areaName(area.getAreaName())
                        .latitude(area.getLatitude())
                        .longitude(area.getLongitude()).build())
                .toList();
    }

    @Transactional(readOnly = true)
    public AreaResponseDto getMyAreaForViceAdmin(AppUser appUser) {
        Role role = appUser.getRole();

        if (!role.equals(Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) &&
            !role.equals(Role.VICE_ADMIN_HEAD_OFFICER)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED_SERVICE.getMessage());
        }

        ViceAdminDetail detail = viceAdminDetailRepository.findById(appUser.getId())
                .orElseThrow(() -> new CustomException("부 관리자 상세정보를 찾을 수 없습니다."));

        Area area = detail.getArea();

        return AreaResponseDto.from(area);
    }

    @Transactional(readOnly = true)
    public AreaDto getAreaById(Long areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        return AreaDto.builder()
                .id(area.getId())
                .areaName(area.getAreaName())
                .latitude(area.getLatitude())
                .longitude(area.getLongitude())
                .build();
    }

    @Transactional
    public void deleteAreaById(AppUser requester, Long areaId) {
        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        }

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        areaRepository.delete(area);
    }
}
