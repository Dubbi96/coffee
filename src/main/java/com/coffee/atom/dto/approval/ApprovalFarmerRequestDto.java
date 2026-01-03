package com.coffee.atom.dto.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class ApprovalFarmerRequestDto {
    @JsonIgnore
    private MultipartFile identificationPhoto;
    private String identificationPhotoUrl;
    private String name;
    @NotNull
    private Long villageHeadId;
    private Long id;

    public ApprovalFarmerRequestDto(MultipartFile identificationPhoto,String name, @NotNull Long villageHeadId) {
        this.identificationPhoto = identificationPhoto;
        this.name = name;
        this.villageHeadId = villageHeadId;
    }
}
