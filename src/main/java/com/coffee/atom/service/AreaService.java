package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.domain.area.Section;
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
        if(!appUser.getRole().equals(Role.ADMIN)) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        Area newArea = Area.builder()
                .areaName(areaRequestDto.getAreaName())
                .longitude(areaRequestDto.getLongitude())
                .latitude(areaRequestDto.getLatitude())
                .build();
        areaRepository.save(newArea);
    }

    @Transactional(readOnly = true)
    public List<AreaSectionResponseDto> getAreaWithSections(AppUser appUser) {
        if(!appUser.getRole().equals(Role.ADMIN)) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        return areaRepository.findAreaWithSections().stream()
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
    public List<AreaResponseDto> getArea(AppUser appUser) {
        if(!appUser.getRole().equals(Role.ADMIN)) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        return areaRepository.findAll().stream()
                .sorted(Comparator.comparing(Area::getAreaName, String.CASE_INSENSITIVE_ORDER))
                .map(area -> AreaResponseDto.builder()
                        .id(area.getId())
                        .areaName(area.getAreaName())
                        .latitude(area.getLatitude())
                        .longitude(area.getLongitude()).build())
                .toList();
    }
}
