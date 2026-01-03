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
    private final SectionRepository sectionRepository;
    private final GCSUtil gcsUtil;
    private final FarmerRepository farmerRepository;
    private final AreaRepository areaRepository;
    private static final int USER_ID_MAX_LENGTH = 50;
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int BANK_NAME_MAX_LENGTH = 255;
    private static final int ACCOUNT_INFO_MAX_LENGTH = 255;

    @Transactional(readOnly = true)
    public SignInResponseDto login(SignInRequestDto accountRequestDto) {
        AppUser appUser = appUserRepository.findByUserId(accountRequestDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));
        if(appUser.getIsApproved() == null || !appUser.getIsApproved()) throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND);
        if (!passwordEncoder.matches(accountRequestDto.getPassword() + appUser.getSalt(), appUser.getPassword()))
            throw new CustomException(ErrorValue.WRONG_PASSWORD);
        return new SignInResponseDto(appUser, jwtProvider.createAccessToken(appUser.getId()));
    }

    @Transactional
    public Long signUp(
            AppUser requester,
            SignUpRequestDto dto) {

        // 총 관리자만 계정 생성 가능
        /*if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }*/

        // ADMIN 권한으로 계정 생성 불가
        /*if (dto.getRole() == Role.ADMIN) {
            throw new CustomException(ErrorValue.ADMIN_CREATION_NOT_ALLOWED);
        }*/

        appUserRepository.findByUsername(dto.getUserId()).ifPresent(appUser -> {
            throw new CustomException(ErrorValue.NICKNAME_ALREADY_EXISTS);
        });

        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(dto.getPassword() + salt);

        AppUser.AppUserBuilder userBuilder = AppUser.builder()
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .role(dto.getRole())
                .isApproved(Boolean.TRUE);

        if (dto.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || dto.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            Area area = areaRepository.findById(dto.getAreaId())
                    .orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND));
            
            // 한 Area에는 각 권한당 한 명씩만 할당 가능
            List<AppUser> existingViceAdmins = appUserRepository.findByAreaAndRole(area, dto.getRole());
            if (!existingViceAdmins.isEmpty()) {
                throw new CustomException(ErrorValue.VICE_ADMIN_ALREADY_EXISTS_IN_AREA);
            }
            
            String idCardUrl = (dto.getIdCardFile() != null) ? uploadFileIfPresent(dto.getIdCardFile(),"vice-admin/", requester) : null;
            
            userBuilder.area(area)
                    .idCardUrl(idCardUrl);
        }

        if (dto.getRole() == Role.VILLAGE_HEAD) {
            Section section = sectionRepository.findById(dto.getSectionId())
                    .orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND));

            String idUrl = (dto.getIdentificationPhotoFile() != null) ? uploadFileIfPresent(dto.getIdentificationPhotoFile() , "village-head/", requester) : null;
            String contractUrl = (dto.getContractFile() != null) ? uploadFileIfPresent(dto.getContractFile(), "village-head/", requester) : null;
            String bankbookUrl = (dto.getBankbookFile() != null) ? uploadFileIfPresent(dto.getBankbookFile(), "village-head/", requester) : null;

            userBuilder.section(section)
                    .bankName(dto.getBankName())
                    .accountInfo(dto.getAccountInfo())
                    .identificationPhotoUrl(idUrl)
                    .contractUrl(contractUrl)
                    .bankbookUrl(bankbookUrl);
        }

        AppUser newUser = userBuilder.build();
        appUserRepository.save(newUser);

        return newUser.getId();
    }

    /**
     * URL 기반 회원가입 (Deprecated된 multipart 회원가입을 대체)
     * - 파일 업로드는 FE가 /gcs API를 통해 선행 처리하고, 여기서는 URL만 저장한다.
     */
    @Transactional
    public Long signUpWithUrls(AppUser requester, SignUpUrlRequestDto dto) {
        appUserRepository.findByUsername(dto.getUserId()).ifPresent(appUser -> {
            throw new CustomException(ErrorValue.NICKNAME_ALREADY_EXISTS);
        });

        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(dto.getPassword() + salt);

        AppUser.AppUserBuilder userBuilder = AppUser.builder()
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .role(dto.getRole())
                .isApproved(Boolean.TRUE);

        if (dto.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || dto.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            Area area = areaRepository.findById(dto.getAreaId())
                    .orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND));

            List<AppUser> existingViceAdmins = appUserRepository.findByAreaAndRole(area, dto.getRole());
            if (!existingViceAdmins.isEmpty()) {
                throw new CustomException(ErrorValue.VICE_ADMIN_ALREADY_EXISTS_IN_AREA);
            }

            // URL만 저장
            userBuilder.area(area)
                    .idCardUrl(dto.getIdentificationPhotoUrl());
        }

        if (dto.getRole() == Role.VILLAGE_HEAD) {
            Section section = sectionRepository.findById(dto.getSectionId())
                    .orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND));

            userBuilder.section(section)
                    .bankName(dto.getBankName())
                    .accountInfo(dto.getAccountInfo())
                    .identificationPhotoUrl(dto.getIdentificationPhotoUrl())
                    .contractUrl(dto.getContractFileUrl())
                    .bankbookUrl(dto.getBankbookPhotoUrl());
        }

        AppUser newUser = userBuilder.build();
        appUserRepository.save(newUser);
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
            String existingUrl = appUser.getIdCardUrl();
            String newFileUrl = uploadIdCardToGCS(appUser, idCardPhoto, existingUrl);
            appUser.updateIdCardUrl(newFileUrl);
        }

        appUserRepository.save(appUser);
    }

    /**
     * URL 기반 내 정보 수정 (Deprecated된 multipart API를 대체)
     */
    @Transactional
    public void updateAppUserStatusWithUrl(AppUser appUser, String username, String password, String idCardUrl) {
        appUser.updateUserName(username);

        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(password + salt);
        appUser.updatePassword(encodedPassword, salt);

        if ((appUser.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER ||
             appUser.getRole() == Role.VICE_ADMIN_HEAD_OFFICER) &&
            StringUtils.hasText(idCardUrl)) {
            // 새 이미지로 바꿨을 때만 기존 파일 삭제
            String existingUrl = appUser.getIdCardUrl();
            if (existingUrl != null && !existingUrl.equals(idCardUrl)) {
                deleteFileIfExists(existingUrl, appUser);
            }
            appUser.updateIdCardUrl(idCardUrl);
        }

        appUserRepository.save(appUser);
    }

    @Transactional(readOnly = true)
    public List<VillageHeadResponseDto> getVillageHeads(AppUser appUser) {
        Role role = appUser.getRole();

        if (role.equals(Role.ADMIN)) {
            return appUserRepository.findAllVillageHeadsWithFarmerCountForAdmin();
        }
        else if (role.equals(Role.VICE_ADMIN_HEAD_OFFICER) || role.equals(Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER)) {
            if (appUser.getArea() == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            Long areaId = appUser.getArea().getId();
            return appUserRepository.findAllVillageHeadsWithFarmerCountByAreaId(areaId);
        }
        else {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }
    }

    @Transactional(readOnly = true)
    public VillageHeadDetailResponseDto getVillageHead(Long villageHeadId) {
        AppUser appUser = appUserRepository.findAppUserByIsApprovedAndId(Boolean.TRUE, villageHeadId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));
        
        if (appUser.getRole() != Role.VILLAGE_HEAD || appUser.getSection() == null) {
            throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND);
        }

        return VillageHeadDetailResponseDto.builder()
                .userId(appUser.getUserId())
                .username(appUser.getUsername())
                .bankName(appUser.getBankName())
                .accountInfo(appUser.getAccountInfo())
                .identificationPhotoUrl(appUser.getIdentificationPhotoUrl())
                .contractFileUrl(appUser.getContractUrl())
                .bankbookPhotoUrl(appUser.getBankbookUrl())
                .areaInfo(VillageHeadDetailResponseDto.AreaInfo.from(appUser.getSection().getArea()))
                .sectionInfo(VillageHeadDetailResponseDto.SectionInfo.from(appUser.getSection()))
                .build();
    }


    @Transactional(readOnly = true)
    public List<ViceAdminsResponseDto> getViceAdmins(AppUser appUser) {
        if (!appUser.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        List<Role> viceAdminRoles = List.of(Role.VICE_ADMIN_HEAD_OFFICER, Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER);
        List<AppUser> viceAdmins = appUserRepository.findAllViceAdminsWithArea(viceAdminRoles);
        return viceAdmins.stream()
                .map(ViceAdminsResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ViceAdminResponseDto getViceAdminDetail(Long viceAdminId, AppUser requester) {
        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        List<Role> viceAdminRoles = List.of(Role.VICE_ADMIN_HEAD_OFFICER, Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER);
        AppUser viceAdmin = appUserRepository.findViceAdminByIdWithArea(viceAdminId, viceAdminRoles)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));

        return ViceAdminResponseDto.builder()
                .id(viceAdmin.getId())
                .userId(viceAdmin.getUserId())
                .username(viceAdmin.getUsername())
                .idCardUrl(viceAdmin.getIdCardUrl())
                .areaInfo(ViceAdminResponseDto.AreaInfo.from(viceAdmin.getArea()))
                .build();
    }

    @Transactional
    public ApprovalVillageHeadRequestDto requestApprovalToCreateVillageHead(AppUser appUser, ApprovalVillageHeadRequestDto approvalVillageHeadRequestDto) {
        // 면장은 Approval을 생성할 수 없음
        if (appUser.getRole() == Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        validateVillageHeadCreateRequest(approvalVillageHeadRequestDto);

        appUserRepository.findByUserId(approvalVillageHeadRequestDto.getUserId()).ifPresent(existing -> {
            throw new CustomException(ErrorValue.USER_ID_ALREADY_EXISTS);
        });
        appUserRepository.findByUsername(approvalVillageHeadRequestDto.getUsername()).ifPresent(existing -> {
            throw new CustomException(ErrorValue.USERNAME_ALREADY_EXISTS);
        });
        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(approvalVillageHeadRequestDto.getPassword() + salt);

        String directory = "village-head/";
        String identificationUrl = hasFile(approvalVillageHeadRequestDto.getIdentificationPhoto())
                ? uploadFileIfPresent(approvalVillageHeadRequestDto.getIdentificationPhoto(), directory, appUser)
                : approvalVillageHeadRequestDto.getIdentificationPhotoUrl();
        String contractUrl = hasFile(approvalVillageHeadRequestDto.getContractFile())
                ? uploadFileIfPresent(approvalVillageHeadRequestDto.getContractFile(), directory, appUser)
                : approvalVillageHeadRequestDto.getContractFileUrl();
        String bankbookUrl = hasFile(approvalVillageHeadRequestDto.getBankbookPhoto())
                ? uploadFileIfPresent(approvalVillageHeadRequestDto.getBankbookPhoto(), directory, appUser)
                : approvalVillageHeadRequestDto.getBankbookPhotoUrl();

        Section section = sectionRepository.findById(approvalVillageHeadRequestDto.getSectionId())
                        .orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND));
        if(!section.getIsApproved()) throw new CustomException(ErrorValue.SECTION_NOT_APPROVED);
        
        // 부관리자의 경우 본인이 배정된 지역의 섹션으로만 면장 생성 가능
        if (appUser.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || 
            appUser.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            if (appUser.getArea() == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            if (section.getArea() == null ||
                !section.getArea().getId().equals(appUser.getArea().getId())) {
                throw new CustomException(ErrorValue.AREA_SECTION_MISMATCH);
            }
        }
        
        AppUser newUser = AppUser.builder()
                .userId(approvalVillageHeadRequestDto.getUserId())
                .username(approvalVillageHeadRequestDto.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .role(Role.VILLAGE_HEAD)
                .section(section)
                .accountInfo(approvalVillageHeadRequestDto.getAccountInfo())
                .bankName(approvalVillageHeadRequestDto.getBankName())
                .bankbookUrl(bankbookUrl)
                .contractUrl(contractUrl)
                .identificationPhotoUrl(identificationUrl)
                .build();
        appUserRepository.save(newUser);
        approvalVillageHeadRequestDto.setId(newUser.getId());
        approvalVillageHeadRequestDto.setIdentificationPhotoUrl(identificationUrl);
        approvalVillageHeadRequestDto.setContractFileUrl(contractUrl);
        approvalVillageHeadRequestDto.setBankbookPhotoUrl(bankbookUrl);
        return approvalVillageHeadRequestDto;
    }

    @Transactional
    public ApprovalFarmerRequestDto requestApprovalToCreateFarmer(AppUser appUser, ApprovalFarmerRequestDto approvalFarmerRequestDto) {
        // 면장은 Approval을 생성할 수 없음
        if (appUser.getRole() == Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        AppUser villageHead = appUserRepository.findById(approvalFarmerRequestDto.getVillageHeadId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));
        if(villageHead.getRole() != Role.VILLAGE_HEAD || villageHead.getIsApproved() == null || !villageHead.getIsApproved()) {
            throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND);
        }

        // 부관리자의 경우 본인이 배정된 지역의 면장 하위에만 농부 생성 가능
        if (appUser.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || 
            appUser.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            if (appUser.getArea() == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            if (villageHead.getSection() == null || 
                villageHead.getSection().getArea() == null ||
                !villageHead.getSection().getArea().getId().equals(appUser.getArea().getId())) {
                throw new CustomException(ErrorValue.FARMER_AREA_MISMATCH);
            }
        }
        String directory = "farmer/";
        String identificationUrl = hasFile(approvalFarmerRequestDto.getIdentificationPhoto())
                ? uploadFileIfPresent(approvalFarmerRequestDto.getIdentificationPhoto(), directory, appUser)
                : approvalFarmerRequestDto.getIdentificationPhotoUrl();
        Farmer farmer = Farmer.builder()
                .name(approvalFarmerRequestDto.getName())
                .villageHead(villageHead)
                .identificationPhotoUrl(identificationUrl)
                .build();
        farmerRepository.save(farmer);
        approvalFarmerRequestDto.setId(farmer.getId());
        approvalFarmerRequestDto.setIdentificationPhotoUrl(identificationUrl);
        return approvalFarmerRequestDto;
    }

    @Transactional
    public ApprovalVillageHeadRequestDto requestApprovalToUpdateVillageHead(AppUser appUser, ApprovalVillageHeadRequestDto dto) {
        validateVillageHeadUpdateRequest(dto);

        AppUser targetUser = appUserRepository.findById(dto.getId())
                .orElseThrow(() -> new CustomException(ErrorValue.APP_USER_NOT_FOUND));

        if (targetUser.getRole() != Role.VILLAGE_HEAD) {
            throw new CustomException(ErrorValue.VILLAGE_HEAD_DETAIL_NOT_FOUND);
        }

        // 부관리자의 경우 본인이 배정된 지역의 면장만 수정 가능
        if (appUser.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || 
            appUser.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            if (appUser.getArea() == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            if (targetUser.getSection() == null || 
                targetUser.getSection().getArea() == null ||
                !targetUser.getSection().getArea().getId().equals(appUser.getArea().getId())) {
                throw new CustomException(ErrorValue.VILLAGE_HEAD_UPDATE_AREA_MISMATCH);
            }
        }

        // 비밀번호 갱신
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            String newSalt = UUID.randomUUID().toString();
            String newPassword = passwordEncoder.encode(dto.getPassword() + newSalt);
            targetUser.updatePassword(newPassword, newSalt);
        }

        String directory = "village-head/";

        // identification (빈 파일/미첨부 시 기존 값 유지)
        if (hasFile(dto.getIdentificationPhoto())) {
            deleteFileIfExists(targetUser.getIdentificationPhotoUrl(), appUser);
            String newIdentificationUrl = uploadFileIfPresent(dto.getIdentificationPhoto(), directory, appUser);
            if (newIdentificationUrl != null) {
                targetUser.updateIdentificationPhotoUrl(newIdentificationUrl);
            }
        } else if (StringUtils.hasText(dto.getIdentificationPhotoUrl())) {
            // URL 기반 요청: 새 이미지로 바꿨을 때만 기존 파일 삭제
            String existingUrl = targetUser.getIdentificationPhotoUrl();
            if (existingUrl != null && !existingUrl.equals(dto.getIdentificationPhotoUrl())) {
                deleteFileIfExists(existingUrl, appUser);
            }
            targetUser.updateIdentificationPhotoUrl(dto.getIdentificationPhotoUrl());
        }

        // contract (빈 파일/미첨부 시 기존 값 유지)
        if (hasFile(dto.getContractFile())) {
            deleteFileIfExists(targetUser.getContractUrl(), appUser);
            String newContractUrl = uploadFileIfPresent(dto.getContractFile(), directory, appUser);
            if (newContractUrl != null) {
                targetUser.updateContractUrl(newContractUrl);
            }
        } else if (StringUtils.hasText(dto.getContractFileUrl())) {
            // URL 기반 요청: 새 이미지로 바꿨을 때만 기존 파일 삭제
            String existingUrl = targetUser.getContractUrl();
            if (existingUrl != null && !existingUrl.equals(dto.getContractFileUrl())) {
                deleteFileIfExists(existingUrl, appUser);
            }
            targetUser.updateContractUrl(dto.getContractFileUrl());
        }

        // bankbook (빈 파일/미첨부 시 기존 값 유지)
        if (hasFile(dto.getBankbookPhoto())) {
            deleteFileIfExists(targetUser.getBankbookUrl(), appUser);
            String newBankbookUrl = uploadFileIfPresent(dto.getBankbookPhoto(), directory, appUser);
            if (newBankbookUrl != null) {
                targetUser.updateBankbookUrl(newBankbookUrl);
            }
        } else if (StringUtils.hasText(dto.getBankbookPhotoUrl())) {
            // URL 기반 요청: 새 이미지로 바꿨을 때만 기존 파일 삭제
            String existingUrl = targetUser.getBankbookUrl();
            if (existingUrl != null && !existingUrl.equals(dto.getBankbookPhotoUrl())) {
                deleteFileIfExists(existingUrl, appUser);
            }
            targetUser.updateBankbookUrl(dto.getBankbookPhotoUrl());
        }

        // 기타 정보 갱신
        if (dto.getAccountInfo() != null) {
            targetUser.updateAccountInfo(dto.getAccountInfo());
        }
        if (dto.getBankName() != null) {
            targetUser.updateBankName(dto.getBankName());
        }

        // Section 갱신
        Section section = sectionRepository.findById(dto.getSectionId())
                .orElseThrow(() -> new CustomException(ErrorValue.SECTION_NOT_FOUND));
        if (!Boolean.TRUE.equals(section.getIsApproved()))
            throw new CustomException(ErrorValue.SECTION_NOT_FOUND);
        
        // 부관리자의 경우 본인이 배정된 지역의 섹션으로만 배정 가능
        if (appUser.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || 
            appUser.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            if (appUser.getArea() == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            if (section.getArea() == null ||
                !section.getArea().getId().equals(appUser.getArea().getId())) {
                throw new CustomException(ErrorValue.VILLAGE_HEAD_SECTION_ASSIGN_MISMATCH);
            }
        }
        
        targetUser.updateSection(section);

        appUserRepository.save(targetUser);

        // 결과 DTO에 URL 세팅
        dto.setIdentificationPhotoUrl(targetUser.getIdentificationPhotoUrl());
        dto.setContractFileUrl(targetUser.getContractUrl());
        dto.setBankbookPhotoUrl(targetUser.getBankbookUrl());

        return dto;
    }

    @Transactional
    public void updateViceAdmin(Long viceAdminId,
                                AppUser requester,
                                ViceAdminRequestDto dto) {
        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        AppUser targetUser = appUserRepository.findById(viceAdminId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));

        if (targetUser.getRole() != Role.VICE_ADMIN_HEAD_OFFICER && 
            targetUser.getRole() != Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND);
        }

        targetUser.updateUserName(dto.getUsername());
        targetUser.updateUserId(dto.getUserId());

        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND));

        // 지역 변경 시 기존 지역의 다른 부관리자가 있는지 확인
        // 같은 지역으로 변경하는 경우는 체크하지 않음
        if (targetUser.getArea() == null || !targetUser.getArea().getId().equals(area.getId())) {
            // 새 지역에 이미 같은 권한의 부관리자가 있는지 확인
            List<AppUser> existingViceAdmins = appUserRepository.findByAreaAndRole(area, targetUser.getRole());
            // 본인을 제외하고 체크
            existingViceAdmins = existingViceAdmins.stream()
                    .filter(u -> !u.getId().equals(targetUser.getId()))
                    .toList();
            if (!existingViceAdmins.isEmpty()) {
                throw new CustomException(ErrorValue.VICE_ADMIN_ALREADY_EXISTS_IN_AREA);
            }
        }

        // ID 카드 파일 업데이트
        if (dto.getIdCardFile() != null && !dto.getIdCardFile().isEmpty()) {
            String newFileUrl = uploadIdCardToGCS(targetUser, dto.getIdCardFile(), targetUser.getIdCardUrl());
            targetUser.updateIdCardUrl(newFileUrl);
        }

        targetUser.updateArea(area);

        appUserRepository.save(targetUser);
    }

    /**
     * URL 기반 부관리자 수정 (Deprecated된 multipart API를 대체)
     */
    @Transactional
    public void updateViceAdminWithUrl(Long viceAdminId, AppUser requester, com.coffee.atom.dto.appuser.ViceAdminUpdateUrlRequestDto dto) {
        if (!requester.getRole().equals(Role.ADMIN)) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        AppUser targetUser = appUserRepository.findById(viceAdminId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));

        if (targetUser.getRole() != Role.VICE_ADMIN_HEAD_OFFICER &&
            targetUser.getRole() != Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND);
        }

        targetUser.updateUserName(dto.getUsername());
        targetUser.updateUserId(dto.getUserId());

        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new CustomException(ErrorValue.AREA_NOT_FOUND));

        if (targetUser.getArea() == null || !targetUser.getArea().getId().equals(area.getId())) {
            List<AppUser> existingViceAdmins = appUserRepository.findByAreaAndRole(area, targetUser.getRole());
            existingViceAdmins = existingViceAdmins.stream()
                    .filter(u -> !u.getId().equals(targetUser.getId()))
                    .toList();
            if (!existingViceAdmins.isEmpty()) {
                throw new CustomException(ErrorValue.VICE_ADMIN_ALREADY_EXISTS_IN_AREA);
            }
        }

        if (StringUtils.hasText(dto.getIdCardUrl())) {
            // 새 이미지로 바꿨을 때만 기존 파일 삭제
            String existingUrl = targetUser.getIdCardUrl();
            if (existingUrl != null && !existingUrl.equals(dto.getIdCardUrl())) {
                deleteFileIfExists(existingUrl, requester);
            }
            targetUser.updateIdCardUrl(dto.getIdCardUrl());
        }

        targetUser.updateArea(area);
        appUserRepository.save(targetUser);
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
                if (appUser.getArea() == null) {
                    throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
                }
                yield ViceAdminMyInfoDto.builder()
                        .appUser(userDto)
                        .idCardUrl(appUser.getIdCardUrl())
                        .area(toAreaDto(appUser.getArea()))
                        .build();
            }

            case VILLAGE_HEAD -> {
                if (appUser.getSection() == null) {
                    throw new CustomException(ErrorValue.VILLAGE_HEAD_NOT_APPROVED);
                }
                Section section = appUser.getSection();
                Area area = section.getArea();
                yield VillageHeadMyInfoDto.builder()
                        .appUser(userDto)
                        .identificationPhotoUrl(appUser.getIdentificationPhotoUrl())
                        .bankName(appUser.getBankName())
                        .accountInfo(appUser.getAccountInfo())
                        .contractUrl(appUser.getContractUrl())
                        .bankbookUrl(appUser.getBankbookUrl())
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
                .orElseThrow(() -> new CustomException(ErrorValue.SUBJECT_NOT_FOUND));

        AppUser villageHead = appUserRepository.findById(dto.getVillageHeadId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));
        if (villageHead.getRole() != Role.VILLAGE_HEAD || villageHead.getIsApproved() == null || !villageHead.getIsApproved())
            throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND);

        String directory = "farmer/";
        String identificationUrl;
        
        if (hasFile(dto.getIdentificationPhoto())) {
            // multipart 파일이 있는 경우: 기존 파일 삭제 후 새 파일 업로드
            deleteFileIfExists(farmer.getIdentificationPhotoUrl(), appUser);
            identificationUrl = uploadFileIfPresent(dto.getIdentificationPhoto(), directory, appUser);
        } else if (StringUtils.hasText(dto.getIdentificationPhotoUrl())) {
            // URL 기반 요청: 새 이미지로 바꿨을 때만 기존 파일 삭제
            String existingUrl = farmer.getIdentificationPhotoUrl();
            if (existingUrl != null && !existingUrl.equals(dto.getIdentificationPhotoUrl())) {
                deleteFileIfExists(existingUrl, appUser);
            }
            identificationUrl = dto.getIdentificationPhotoUrl();
        } else {
            // URL이 제공되지 않은 경우 기존 URL 유지
            identificationUrl = farmer.getIdentificationPhotoUrl();
        }

        // 기존 농부 데이터를 기반으로 수정 DTO 구성
        dto.setIdentificationPhotoUrl(identificationUrl);
        return dto;
    }

    private void validateVillageHeadCreateRequest(ApprovalVillageHeadRequestDto dto) {
        if (dto == null) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
        if (!StringUtils.hasText(dto.getUserId())) {
            throw new CustomException(ErrorValue.USER_ID_REQUIRED);
        }
        if (dto.getUserId().length() > USER_ID_MAX_LENGTH) {
            throw new CustomException(ErrorValue.USER_ID_TOO_LONG);
        }
        if (!StringUtils.hasText(dto.getUsername())) {
            throw new CustomException(ErrorValue.USERNAME_REQUIRED);
        }
        if (dto.getUsername().length() > USERNAME_MAX_LENGTH) {
            throw new CustomException(ErrorValue.USERNAME_TOO_LONG);
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new CustomException(ErrorValue.PASSWORD_REQUIRED);
        }
        if (StringUtils.hasText(dto.getBankName()) && dto.getBankName().length() > BANK_NAME_MAX_LENGTH) {
            throw new CustomException(ErrorValue.BANK_NAME_TOO_LONG);
        }
        if (StringUtils.hasText(dto.getAccountInfo()) && dto.getAccountInfo().length() > ACCOUNT_INFO_MAX_LENGTH) {
            throw new CustomException(ErrorValue.ACCOUNT_INFO_TOO_LONG);
        }
    }

    private void validateVillageHeadUpdateRequest(ApprovalVillageHeadRequestDto dto) {
        if (dto == null) {
            throw new CustomException(ErrorValue.JSON_PROCESSING_ERROR);
        }
        if (dto.getId() == null) {
            throw new CustomException(ErrorValue.VILLAGE_HEAD_ID_REQUIRED);
        }
        if (StringUtils.hasText(dto.getUserId()) && dto.getUserId().length() > USER_ID_MAX_LENGTH) {
            throw new CustomException(ErrorValue.USER_ID_TOO_LONG);
        }
        if (StringUtils.hasText(dto.getUsername()) && dto.getUsername().length() > USERNAME_MAX_LENGTH) {
            throw new CustomException(ErrorValue.USERNAME_TOO_LONG);
        }
        if (StringUtils.hasText(dto.getBankName()) && dto.getBankName().length() > BANK_NAME_MAX_LENGTH) {
            throw new CustomException(ErrorValue.BANK_NAME_TOO_LONG);
        }
        if (StringUtils.hasText(dto.getAccountInfo()) && dto.getAccountInfo().length() > ACCOUNT_INFO_MAX_LENGTH) {
            throw new CustomException(ErrorValue.ACCOUNT_INFO_TOO_LONG);
        }
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
            throw new CustomException(ErrorValue.ID_CARD_UPLOAD_FAILED);
        }
    }

    private void deleteFileIfExists(String fileUrl, AppUser appUser) {
        if (fileUrl != null && !fileUrl.isBlank()) {
            gcsUtil.deleteFileFromGCS(fileUrl, appUser); // 내부에서 로그도 비동기 기록됨
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String uploadFileIfPresent(MultipartFile file, String directory, AppUser uploader) {
        if (file != null && !file.isEmpty()) {
            try {
                return gcsUtil.uploadFileToGCS(directory, file, uploader);
            } catch (IOException e) {
                throw new CustomException(ErrorValue.UNKNOWN_ERROR);
            }
        }
        return null;
    }
}
