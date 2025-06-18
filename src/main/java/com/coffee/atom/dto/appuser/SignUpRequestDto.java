package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Data
public class SignUpRequestDto {
    private String userId;
    private String username;
    private String password;
    private Role role;
    private Long areaId;
    private Long sectionId;
    private String bankName;
    private String accountInfo;

    @JsonIgnore
    private MultipartFile idCardFile;

    @JsonIgnore
    private MultipartFile identificationPhotoFile;

    @JsonIgnore
    private MultipartFile contractFile;

    @JsonIgnore
    private MultipartFile bankbookFile;
}