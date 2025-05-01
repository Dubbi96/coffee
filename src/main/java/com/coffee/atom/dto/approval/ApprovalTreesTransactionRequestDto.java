package com.coffee.atom.dto.approval;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApprovalTreesTransactionRequestDto {
    private Long id;
    @Parameter(description = "나무 종")
    private String species;
    @Parameter(description = "수령 한 농부 ID")
    private Long farmerId;
    @Parameter(description = "나무 수량")
    private Long quantity;
    @Parameter(description = "수령 일자")
    private LocalDate receivedDate;
}
