package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.config.security.JwtProvider;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.appuser.*;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.ApprovalFarmerRequestDto;
import com.coffee.atom.dto.approval.ApprovalVillageHeadRequestDto;
import com.coffee.atom.dto.appuser.*;
import com.coffee.atom.dto.area.AreaDto;
import com.coffee.atom.dto.area.SectionDto;
import com.coffee.atom.util.GCSUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ViceAdminDetailRepository viceAdminDetailRepository;
    private final VillageHeadDetailRepository villageHeadDetailRepository;
    private final SectionRepository sectionRepository;
    private final GCSUtil gcsUtil;
    private final FarmerRepository farmerRepository;
    private final AreaRepository areaRepository;

    @Transactional(readOnly = true)
    public SignInResponseDto login(SignInRequestDto accountRequestDto) {
        AppUser appUser = appUserRepository.findByUserId(accountRequestDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        if(appUser.getIsApproved() == null || !appUser.getIsApproved()) throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage());
        if (!passwordEncoder.matches(accountRequestDto.getPassword() + appUser.getSalt(), appUser.getPassword()))
            throw new CustomException("올바르지 않은 아이디 및 비밀번호입니다.");
        return new SignInResponseDto(appUser, jwtProvider.createAccessToken(appUser.getId()));
    }

    @Transactional
    public Long signUp(
            AppUser requester,
            SignUpRequestDto dto) {

        appUserRepository.findByUsername(dto.getUserId()).ifPresent(appUser -> {
            throw new IllegalArgumentException(ErrorValue.NICKNAME_ALREADY_EXISTS.toString());
        });

        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(dto.getPassword() + salt);

        AppUser newUser = AppUser.builder()
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .role(dto.getRole())
                .isApproved(Boolean.TRUE)
                .build();

        appUserRepository.save(newUser);

        if (dto.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || dto.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            Area area = areaRepository.findById(dto.getAreaId())
                    .orElseThrow(() -> new CustomException("존재하지 않는 지역입니다."));
            String idCardUrl = (dto.getIdCardFile() != null) ? uploadFileIfPresent(dto.getIdCardFile(),"vice-admin/", requester) : null;

            ViceAdminDetail detail = ViceAdminDetail.builder()
                    .appUser(newUser)
                    .area(area)
                    .idCardUrl(idCardUrl)
                    .build();

            viceAdminDetailRepository.save(detail);
        }

        if (dto.getRole() == Role.VILLAGE_HEAD) {
            Section section = sectionRepository.findById(dto.getSectionId())
                    .orElseThrow(() -> new CustomException("존재하지 않는 섹션입니다."));

            String idUrl = (dto.getIdentificationPhotoFile() != null) ? uploadFileIfPresent(dto.getIdentificationPhotoFile() , "village-head/", requester) : null;
            String contractUrl = (dto.getContractFile() != null) ? uploadFileIfPresent(dto.getContractFile(), "village-head/", requester) : null;
            String bankbookUrl = (dto.getBankbookFile() != null) ? uploadFileIfPresent(dto.getBankbookFile(), "village-head/", requester) : null;

            VillageHeadDetail detail = VillageHeadDetail.builder()
                    .appUser(newUser)
                    .section(section)
                    .bankName(dto.getBankName())
                    .accountInfo(dto.getAccountInfo())
                    .identificationPhotoUrl(idUrl)
                    .contractUrl(contractUrl)
                    .bankbookUrl(bankbookUrl)
                    .isApproved(true)
                    .build();

            villageHeadDetailRepository.save(detail);
        }

        return newUser.getId();
    }

    @Transactional
    public void updateAppUserStatus(AppUser appUser, String username, String password, MultipartFile idCardPhoto) {
        appUser.updateUserName(username);

        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password + salt);
        appUser.updatePassword(encodedPassword, salt);

        if ((appUser.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER ||
             appUser.getRole() == Role.VICE_ADMIN_HEAD_OFFICER) &&
            idCardPhoto != null) {

            ViceAdminDetail detail = viceAdminDetailRepository.findById(appUser.getId())
                    .map(existing -> {
                        String existingUrl = existing.getIdCardUrl();
                        String newFileUrl = uploadIdCardToGCS(appUser, idCardPhoto, existingUrl);
                        return existing.updateIdCardUrl(newFileUrl);
                    })
                    .orElseGet(() -> {
                        String newFileUrl = uploadIdCardToGCS(appUser, idCardPhoto, null);
                        return ViceAdminDetail.builder()
                                .appUser(appUser)
                                .idCardUrl(newFileUrl)
                                .build();
                    });

            viceAdminDetailRepository.save(detail);
        }

        appUserRepository.save(appUser);
    }

    @Transactional(readOnly = true)
    public List<VillageHeadResponseDto> getVillageHeads(AppUser appUser) {
        Role role = appUser.getRole();

        if (role.equals(Role.ADMIN)) {
            return villageHeadDetailRepository.findAllWithFarmerCountForAdmin();
        }
        else if (role.equals(Role.VICE_ADMIN_HEAD_OFFICER) || role.equals(Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER)) {
            // ➤ ViceAdminDetail을 기반으로 areaId 추출
            ViceAdminDetail viceAdminDetail = viceAdminDetailRepository.findByAppUserId(appUser.getId())
                    .orElseThrow(() -> new CustomException("부 관리자 정보가 존재하지 않습니다."));

            Long areaId = viceAdminDetail.getArea().getId();

            return villageHeadDetailRepository.findAllWithFarmerCountByAreaId(areaId);
        }
        else {
            throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public VillageHeadDetailResponseDto getVillageHead(Long villageHeadId) {
        VillageHeadDetail villageHeadDetail = villageHeadDetailRepository.findVillageHeadDetailByIsApprovedAndId(Boolean.TRUE, villageHeadId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        AppUser appUser = appUserRepository.findAppUserByIsApprovedAndId(Boolean.TRUE, villageHeadId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));

        return VillageHeadDetailResponseDto.builder()
                .userId(appUser.getUserId())
                .username(appUser.getUsername())
                .bankName(villageHeadDetail.getBankName())
                .accountInfo(villageHeadDetail.getAccountInfo())
                .identificationPhotoUrl(villageHeadDetail.getIdentificationPhotoUrl())
                .contractFileUrl(villageHeadDetail.getContractUrl())
                .bankbookPhotoUrl(villageHeadDetail.getBankbookUrl())
                .areaInfo(VillageHeadDetailResponseDto.AreaInfo.from(villageHeadDetail.getSection().getArea()))
                .sectionInfo(VillageHeadDetailResponseDto.SectionInfo.from((villageHeadDetail.getSection())))
                .build();
    }


    @Transactional(readOnly = true)
    public List<ViceAdminsResponseDto> getViceAdmins(AppUser appUser) {
        if (!appUser.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        }

        List<ViceAdminDetail> viceAdmins = viceAdminDetailRepository.findAllWithAppUserAndArea();
        return viceAdmins.stream()
                .map(ViceAdminsResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ViceAdminResponseDto getViceAdminDetail(Long viceAdminId, AppUser requester) {
        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        }

        ViceAdminDetail detail = viceAdminDetailRepository.findByIdWithAppUserAndArea(viceAdminId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));

        return ViceAdminResponseDto.builder()
                .id(detail.getId())
                .userId(detail.getAppUser().getUserId())
                .username(detail.getAppUser().getUsername())
                .idCardUrl(detail.getIdCardUrl())
                .areaInfo(ViceAdminResponseDto.AreaInfo.from(detail.getArea()))
                .build();
    }

    @Transactional
    public ApprovalVillageHeadRequestDto requestApprovalToCreateVillageHead(AppUser appUser, ApprovalVillageHeadRequestDto approvalVillageHeadRequestDto) {
        appUserRepository.findByUsername(approvalVillageHeadRequestDto.getUserId()).ifPresent(villageHead -> {
            throw new IllegalArgumentException(ErrorValue.NICKNAME_ALREADY_EXISTS.toString());
        });
        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(approvalVillageHeadRequestDto.getPassword() + salt);

        AppUser newUser = AppUser.builder()
                .userId(approvalVillageHeadRequestDto.getUserId())
                .username(approvalVillageHeadRequestDto.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .role(Role.VILLAGE_HEAD)
                .build();

        String directory = "village-head/";
        String identificationUrl = uploadFileIfPresent(approvalVillageHeadRequestDto.getIdentificationPhoto(), directory, appUser);
        String contractUrl = uploadFileIfPresent(approvalVillageHeadRequestDto.getContractFile(), directory, appUser);
        String bankbookUrl = uploadFileIfPresent(approvalVillageHeadRequestDto.getBankbookPhoto(), directory, appUser);

        Section section = sectionRepository.findById(approvalVillageHeadRequestDto.getSectionId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Section 입니다."));
        if(!section.getIsApproved()) throw new CustomException(ErrorValue.SECTION_NOT_FOUND.getMessage());
        VillageHeadDetail newVillageHead = VillageHeadDetail.builder()
                .appUser(newUser)
                .accountInfo(approvalVillageHeadRequestDto.getAccountInfo())
                .bankName(approvalVillageHeadRequestDto.getBankName())
                .bankbookUrl(bankbookUrl)
                .contractUrl(contractUrl)
                .identificationPhotoUrl(identificationUrl)
                .section(section)
                .build();
        appUserRepository.save(newUser);
        villageHeadDetailRepository.save(newVillageHead);
        approvalVillageHeadRequestDto.setId(newUser.getId());
        approvalVillageHeadRequestDto.setIdentificationPhotoUrl(identificationUrl);
        approvalVillageHeadRequestDto.setContractFileUrl(contractUrl);
        approvalVillageHeadRequestDto.setBankbookPhotoUrl(bankbookUrl);
        return approvalVillageHeadRequestDto;
    }

    @Transactional
    public ApprovalFarmerRequestDto requestApprovalToCreateFarmer(AppUser appUser, ApprovalFarmerRequestDto approvalFarmerRequestDto) {
        VillageHeadDetail villageHeadDetail = villageHeadDetailRepository.findById(approvalFarmerRequestDto.getVillageHeadId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        if(!villageHeadDetail.getIsApproved()) throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage());
        String directory = "farmer/";
        String identificationUrl = uploadFileIfPresent(approvalFarmerRequestDto.getIdentificationPhoto(), directory, appUser);
        Farmer farmer = Farmer.builder()
                .name(approvalFarmerRequestDto.getName())
                .villageHead(villageHeadDetail)
                .identificationPhotoUrl(identificationUrl)
                .build();
        farmerRepository.save(farmer);
        approvalFarmerRequestDto.setId(farmer.getId());
        approvalFarmerRequestDto.setIdentificationPhotoUrl(identificationUrl);
        return approvalFarmerRequestDto;
    }

    @Transactional
    public ApprovalVillageHeadRequestDto requestApprovalToUpdateVillageHead(AppUser appUser, ApprovalVillageHeadRequestDto dto) {
        AppUser targetUser = appUserRepository.findById(dto.getId())
                .orElseThrow(() -> new CustomException("수정 대상 면장 사용자를 찾을 수 없습니다."));

        VillageHeadDetail villageHead = villageHeadDetailRepository.findByAppUser(targetUser)
                .orElseThrow(() -> new CustomException("면장 세부정보를 찾을 수 없습니다."));

        // 비밀번호 갱신
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String newSalt = UUID.randomUUID().toString();
            String newPassword = passwordEncoder.encode(dto.getPassword() + newSalt);
            targetUser.updatePassword(newPassword, newSalt);
        }

        String directory = "village-head/";

        // identification
        deleteFileIfExists(villageHead.getIdentificationPhotoUrl(), appUser);
        String newIdentificationUrl = uploadFileIfPresent(dto.getIdentificationPhoto(), directory, appUser);
        villageHead.updateIdentificationPhotoUrl(newIdentificationUrl);

        // contract
        deleteFileIfExists(villageHead.getContractUrl(), appUser);
        String newContractUrl = uploadFileIfPresent(dto.getContractFile(), directory, appUser);
        villageHead.updateContractUrl(newContractUrl);

        // bankbook
        deleteFileIfExists(villageHead.getBankbookUrl(), appUser);
        String newBankbookUrl = uploadFileIfPresent(dto.getBankbookPhoto(), directory, appUser);
        villageHead.updateBankbookUrl(newBankbookUrl);

        // 기타 정보 갱신
        villageHead.updateAccountInfo(dto.getAccountInfo());
        villageHead.updateBankName(dto.getBankName());

        // Section 갱신
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Section 입니다."));
        if (!Boolean.TRUE.equals(section.getIsApproved()))
            throw new CustomException(ErrorValue.SECTION_NOT_FOUND.getMessage());
        villageHead.updateSection(section);

        appUserRepository.save(targetUser);
        villageHeadDetailRepository.save(villageHead);

        // 결과 DTO에 URL 세팅
        dto.setIdentificationPhotoUrl(villageHead.getIdentificationPhotoUrl());
        dto.setContractFileUrl(villageHead.getContractUrl());
        dto.setBankbookPhotoUrl(villageHead.getBankbookUrl());

        return dto;
    }

    @Transactional
    public void updateViceAdmin(Long viceAdminId,
                                AppUser requester,
                                ViceAdminRequestDto dto) {
        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        }

        AppUser targetUser = appUserRepository.findById(viceAdminId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));

        targetUser.updateUserName(dto.getUsername());
        targetUser.updateUserId(dto.getUserId());

        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new CustomException("존재하지 않는 지역입니다."));

        ViceAdminDetail detail = viceAdminDetailRepository.findById(viceAdminId)
            .map(existing -> {
                String newFileUrl = dto.getIdCardFile() != null && !dto.getIdCardFile().isEmpty()
                        ? uploadIdCardToGCS(targetUser, dto.getIdCardFile(), existing.getIdCardUrl())
                        : existing.getIdCardUrl();
                return existing.updateIdCardUrl(newFileUrl);
            })
            .orElseGet(() -> {
                String newFileUrl = dto.getIdCardFile() != null && !dto.getIdCardFile().isEmpty()
                        ? uploadIdCardToGCS(targetUser, dto.getIdCardFile(), null)
                        : null;
                return ViceAdminDetail.builder()
                        .appUser(targetUser)
                        .area(area)
                        .idCardUrl(newFileUrl)
                        .build();
            });

        detail.updateArea(area);

        appUserRepository.save(targetUser);
        viceAdminDetailRepository.save(detail);
    }

    @Transactional(readOnly = true)
    public Object getMyInfo(AppUser appUser) {
        AppUserInfoDto userDto = AppUserInfoDto.builder()
                .id(appUser.getId())
                .userId(appUser.getUserId())
                .username(appUser.getUsername())
                .role(appUser.getRole())
                .build();

        return switch (appUser.getRole()) {
            case ADMIN -> AdminMyInfoDto.builder()
                    .appUser(userDto)
                    .build();

            case VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER -> {
                ViceAdminDetail detail = viceAdminDetailRepository.findById(appUser.getId())
                        .orElseThrow(() -> new CustomException("부관리자 정보가 없습니다."));
                yield ViceAdminMyInfoDto.builder()
                        .appUser(userDto)
                        .idCardUrl(detail.getIdCardUrl())
                        .area(toAreaDto(detail.getArea()))
                        .build();
            }

            case VILLAGE_HEAD -> {
                VillageHeadDetail detail = villageHeadDetailRepository
                        .findVillageHeadDetailByIsApprovedAndId(Boolean.TRUE, appUser.getId())
                        .orElseThrow(() -> new CustomException("승인된 면장 정보가 없습니다."));
                Section section = detail.getSection();
                Area area = section.getArea();
                yield VillageHeadMyInfoDto.builder()
                        .appUser(userDto)
                        .identificationPhotoUrl(detail.getIdentificationPhotoUrl())
                        .bankName(detail.getBankName())
                        .accountInfo(detail.getAccountInfo())
                        .contractUrl(detail.getContractUrl())
                        .bankbookUrl(detail.getBankbookUrl())
                        .section(toSectionDto(section))
                        .area(toAreaDto(area))
                        .build();
            }
        };
    }

    private AreaDto toAreaDto(Area area) {
        return AreaDto.builder()
                .id(area.getId())
                .areaName(area.getAreaName())
                .longitude(area.getLongitude())
                .latitude(area.getLatitude())
                .build();
    }

    private SectionDto toSectionDto(Section section) {
        return SectionDto.builder()
                .id(section.getId())
                .sectionName(section.getSectionName())
                .longitude(section.getLongitude())
                .latitude(section.getLatitude())
                .build();
    }

    @Transactional
    public ApprovalFarmerRequestDto requestApprovalToUpdateFarmer(AppUser appUser, ApprovalFarmerRequestDto dto) {
        Farmer farmer = farmerRepository.findById(dto.getId())
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND.getMessage()));

        VillageHeadDetail villageHeadDetail = villageHeadDetailRepository.findById(dto.getVillageHeadId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        if (!villageHeadDetail.getIsApproved())
            throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage());

        String directory = "farmer/";
        String identificationUrl = uploadFileIfPresent(dto.getIdentificationPhoto(), directory, appUser);

        // 기존 농부 데이터를 기반으로 수정 DTO 구성
        dto.setIdentificationPhotoUrl(identificationUrl != null ? identificationUrl : farmer.getIdentificationPhotoUrl());
        return dto;
    }

    /**
     * GCS에 ID 카드 업로드 후 URL 반환
     */
    private String uploadIdCardToGCS(AppUser appUser, MultipartFile file, String previousFileUrl) {
        try {
            if (StringUtils.hasText(previousFileUrl)) {
                gcsUtil.deleteFileFromGCS(previousFileUrl, appUser);
            }
            return gcsUtil.uploadFileToGCS("vice-admin/", file, appUser);
        } catch (IOException e) {
            throw new CustomException("ID 카드 업로드 실패");
        }
    }

    private void deleteFileIfExists(String fileUrl, AppUser appUser) {
        if (fileUrl != null && !fileUrl.isBlank()) {
            gcsUtil.deleteFileFromGCS(fileUrl, appUser); // 내부에서 로그도 비동기 기록됨
        }
    }

    private String uploadFileIfPresent(MultipartFile file, String directory, AppUser uploader) {
        if (file != null && !file.isEmpty()) {
            try {
                return gcsUtil.uploadFileToGCS(directory, file, uploader);
            } catch (IOException e) {
                throw new CustomException("파일 업로드 실패: " + file.getOriginalFilename());
            }
        }
        return null;
    }
}
