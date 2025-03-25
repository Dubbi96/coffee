package com.coffee.atom.dto.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class ApprovalVillageHeadRequestDto {
    private String userId;
    private String password;
    private String username;
    private String bankName;
    private String accountInfo;
    @JsonIgnore
    private MultipartFile identificationPhoto;
    @JsonIgnore
    private MultipartFile contractFile;
    @JsonIgnore
    private MultipartFile bankbookPhoto;
    @NotNull
    private Long SectionId;
}
