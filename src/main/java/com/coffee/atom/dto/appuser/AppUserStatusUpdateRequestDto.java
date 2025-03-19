package com.coffee.atom.dto.appuser;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppUserStatusUpdateRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String idCardUrl;
}
