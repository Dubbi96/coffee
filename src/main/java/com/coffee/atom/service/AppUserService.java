package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.config.security.JwtProvider;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.appuser.*;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.ApprovalFarmerRequestDto;
import com.coffee.atom.dto.approval.ApprovalVillageHeadRequestDto;
import com.coffee.atom.dto.appuser.*;
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
    public Long signUp(SignUpRequestDto authRequestDto) {
        appUserRepository.findByUsername(authRequestDto.getUserId()).ifPresent(appUser -> {
            throw new IllegalArgumentException(ErrorValue.NICKNAME_ALREADY_EXISTS.toString());
        });
        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(authRequestDto.getPassword() + salt);

        AppUser newUser = AppUser.builder()
                .userId(authRequestDto.getUserId())
                .username(authRequestDto.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .role(authRequestDto.getRole())
                .isApproved(Boolean.TRUE)
                .build();

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

    /**
     * GCS에 ID 카드 업로드 후 URL 반환
     */
    private String uploadIdCardToGCS(AppUser appUser, MultipartFile file, String previousFileUrl) {
        try {
            if (StringUtils.hasText(previousFileUrl)) {
                gcsUtil.deleteFileFromGCS(previousFileUrl, appUser);
            }
            return gcsUtil.uploadFileToGCS("id_cards", file, appUser);
        } catch (IOException e) {
            throw new CustomException("ID 카드 업로드 실패");
        }
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
                .accountInfo(villageHeadDetail.getAccountInfo())
                .identificationPhotoUrl(villageHeadDetail.getIdentificationPhotoUrl())
                .contractFileUrl(villageHeadDetail.getContractUrl())
                .bankbookPhotoUrl(villageHeadDetail.getBankbookUrl())
                .sectionInfo(VillageHeadDetailResponseDto.SectionInfo.from((villageHeadDetail.getSection())))
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