package com.coffee.atom.dto.appuser;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AppUserStatusUpdateRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private MultipartFile idCardPhoto;
}
