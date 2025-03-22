package com.coffee.atom.dto.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class ApprovalFarmerRequestDto {
    @JsonIgnore
    private MultipartFile identificationPhoto;
    private String name;
    @NotNull
    private Long villageHeadId;
}
