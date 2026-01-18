package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
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

@Service
@RequiredArgsConstructor
public class AreaService {
    private final AreaRepository areaRepository;

    @Transactional
    public void saveArea(AppUser appUser, AreaRequestDto areaRequestDto) {
        if(!appUser.getRole().equals(Role.ADMIN)) throw new CustomException(ErrorValue.UNAUTHORIZED);
        Area newArea = Area.builder()
                .areaName(areaRequestDto.getAreaName())
                .longitude(areaRequestDto.getLongitude())
                .latitude(areaRequestDto.getLatitude())
                .build();
        areaRepository.save(newArea);
    }

    @Transactional(readOnly = true)
    public List<AreaSectionResponseDto> getAreasWithSections(AppUser appUser) {
        Role role = appUser.getRole();
        
        // 면장은 조회 불가
        if (role == Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }
        
        List<Area> areas;
        
        if (role == Role.ADMIN) {
            // 총 관리자: 모든 지역 및 섹션 조회
            areas = areaRepository.findAreasWithSections();
        } else if (role == Role.VICE_ADMIN_HEAD_OFFICER || role == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            // 부 관리자: 본인이 속한 Area와 그 Area 내의 Section만 조회
            Area userArea = appUser.getArea();
            if (userArea == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            areas = areaRepository.findAreaWithSections(userArea.getId());
        } else {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }
        
        return areas.stream()
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
        Role role = appUser.getRole();
        
        // 면장은 조회 불가
        if (role == Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }
        
        List<Area> areas;
        
        if (role == Role.ADMIN) {
            // 총 관리자: 모든 지역 조회
            areas = areaRepository.findAll();
        } else if (role == Role.VICE_ADMIN_HEAD_OFFICER || role == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            // 부 관리자: 본인이 배정된 지역만 조회
            Area userArea = appUser.getArea();
            if (userArea == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            // Lazy proxy 초기화를 위해 필요한 필드들을 명시적으로 접근
            Long areaId = userArea.getId();
            Area area = areaRepository.findById(areaId)
                    .orElseThrow(() -> new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND));
            areas = List.of(area);
        } else {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }
        
        return areas.stream()
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
            throw new CustomException(ErrorValue.UNAUTHORIZED_SERVICE);
        }

        Area area = appUser.getArea();
        if (area == null) {
            throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
        }

        return AreaResponseDto.from(area);
    }

    @Transactional(readOnly = true)
    public AreaDto getAreaById(Long areaId) {
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));

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
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));

        areaRepository.delete(area);
    }
}
