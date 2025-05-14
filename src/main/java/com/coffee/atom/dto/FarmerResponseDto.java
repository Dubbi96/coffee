package com.coffee.atom.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class FarmerResponseDto {
    private String sectionName;
    private String farmerName;
    private String identificationPhotoUrl;
    private List<TreesTransactionDetail> treesTransactions;

    @Data
    @Builder
    public static class TreesTransactionDetail{
        private String species;
        private LocalDate receivedDate;
        private Long quantity;
    }
}
