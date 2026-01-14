package com.coffee.atom.dto.approval;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class ApprovalFarmerRequestDto {
    @JsonIgnore
    private MultipartFile identificationPhoto;
    
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String identificationPhotoUrl;
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    private String name;
    
    @NotNull(message = "면장 ID는 필수입니다")
    @Positive(message = "면장 ID는 양수여야 합니다")
    private Long villageHeadId;
    
    private Long id;

    public ApprovalFarmerRequestDto(MultipartFile identificationPhoto, String name, Long villageHeadId) {
        this.identificationPhoto = identificationPhoto;
        this.name = name;
        this.villageHeadId = villageHeadId;
    }
}
