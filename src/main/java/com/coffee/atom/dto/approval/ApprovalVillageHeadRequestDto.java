package com.coffee.atom.dto.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class ApprovalVillageHeadRequestDto {
    private Long id;
    
    @Size(max = 50, message = "사용자 ID는 50자 이하여야 합니다")
    private String userId;
    
    @Size(max = 255, message = "비밀번호는 255자 이하여야 합니다")
    private String password;
    
    @Size(max = 50, message = "사용자명은 50자 이하여야 합니다")
    private String username;
    
    @Size(max = 255, message = "은행명은 255자 이하여야 합니다")
    private String bankName;
    
    @Size(max = 255, message = "계좌정보는 255자 이하여야 합니다")
    private String accountInfo;
    
    @JsonIgnore
    private MultipartFile identificationPhoto;
    
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String identificationPhotoUrl;
    
    @JsonIgnore
    private MultipartFile contractFile;
    
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String contractFileUrl;
    
    @JsonIgnore
    private MultipartFile bankbookPhoto;
    
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String bankbookPhotoUrl;
    
    @NotNull(message = "Section ID는 필수입니다")
    @Positive(message = "Section ID는 양수여야 합니다")
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
