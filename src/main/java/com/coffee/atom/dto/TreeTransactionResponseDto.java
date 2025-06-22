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
    private String species;
    private Long quantity;
    private LocalDate receivedDate;

    public static TreeTransactionResponseDto from(TreesTransaction tx) {
        return TreeTransactionResponseDto.builder()
                .id(tx.getId())
                .farmerId(tx.getFarmer().getId())
                .farmerName(tx.getFarmer().getName())
                .species(tx.getSpecies())
                .quantity(tx.getQuantity())
                .receivedDate(tx.getReceivedDate())
                .build();
    }
}