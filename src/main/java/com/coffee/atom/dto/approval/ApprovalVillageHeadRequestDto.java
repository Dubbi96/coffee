package com.coffee.atom.dto.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ApprovalVillageHeadRequestDto {
    private Long id;
    private String userId;
    private String password;
    private String username;
    private String bankName;
    private String accountInfo;
    @JsonIgnore
    private MultipartFile identificationPhoto;
    private String identificationPhotoUrl;
    @JsonIgnore
    private MultipartFile contractFile;
    private String contractFileUrl;
    @JsonIgnore
    private MultipartFile bankbookPhoto;
    private String bankbookPhotoUrl;
    @NotNull
    private Long SectionId;

    public ApprovalVillageHeadRequestDto(Long id, String userId, String password, String username, String bankName, String accountInfo, MultipartFile identificationPhoto, MultipartFile contractFile, MultipartFile bankbookPhoto, Long SectionId) {
        this.id = id;
        this.userId = userId;
        this.password = password;
        this.username = username;
        this.bankName = bankName;
        this.accountInfo = accountInfo;
        this.identificationPhoto = identificationPhoto;
        this.contractFile = contractFile;
        this.bankbookPhoto = bankbookPhoto;
        this.SectionId = SectionId;
    }

    public ApprovalVillageHeadRequestDto(String userId, String password, String username, String bankName, String accountInfo, MultipartFile identificationPhoto, MultipartFile contractFile, MultipartFile bankbookPhoto, Long SectionId) {
        this.userId = userId;
        this.password = password;
        this.username = username;
        this.bankName = bankName;
        this.accountInfo = accountInfo;
        this.identificationPhoto = identificationPhoto;
        this.contractFile = contractFile;
        this.bankbookPhoto = bankbookPhoto;
        this.SectionId = SectionId;
    }
}
