package com.coffee.atom.dto;

import com.coffee.atom.domain.TreesTransaction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TreeTransactionResponseDto {
    private Long farmerId;
    private String species;
    private Long quantity;
    private LocalDate receivedDate;

    public static TreeTransactionResponseDto from(TreesTransaction tx) {
        return TreeTransactionResponseDto.builder()
                .farmerId(tx.getFarmer().getId())
                .species(tx.getSpecies())
                .quantity(tx.getQuantity())
                .receivedDate(tx.getReceivedDate())
                .build();
    }
}