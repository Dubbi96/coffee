package com.coffee.atom.dto;

import com.coffee.atom.domain.TreesTransaction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TreeTransactionResponseDto {
    private Long id;
    private Long farmerId;
    private String farmerName;
    private Long villageHeadId;
    private String villageHeadName;
    private Long areaId;
    private String areaName;
    private Long sectionId;
    private String sectionName;
    private String species;
    private Long quantity;
    private LocalDate receivedDate;

    public static TreeTransactionResponseDto from(TreesTransaction tx) {
        return TreeTransactionResponseDto.builder()
                .id(tx.getId())
                .farmerId(tx.getFarmer().getId())
                .farmerName(tx.getFarmer().getName())
                .villageHeadId(tx.getFarmer().getVillageHead().getId())
                .villageHeadName(tx.getFarmer().getVillageHead().getUsername())
                .areaId(tx.getFarmer().getVillageHead().getSection() != null && tx.getFarmer().getVillageHead().getSection().getArea() != null 
                        ? tx.getFarmer().getVillageHead().getSection().getArea().getId() : null)
                .areaName(tx.getFarmer().getVillageHead().getSection() != null && tx.getFarmer().getVillageHead().getSection().getArea() != null 
                        ? tx.getFarmer().getVillageHead().getSection().getArea().getAreaName() : null)
                .sectionId(tx.getFarmer().getVillageHead().getSection() != null ? tx.getFarmer().getVillageHead().getSection().getId() : null)
                .sectionName(tx.getFarmer().getVillageHead().getSection() != null ? tx.getFarmer().getVillageHead().getSection().getSectionName() : null)
                .species(tx.getSpecies())
                .quantity(tx.getQuantity())
                .receivedDate(tx.getReceivedDate())
                .build();
    }
}