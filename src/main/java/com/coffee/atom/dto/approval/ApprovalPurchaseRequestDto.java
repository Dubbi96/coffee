package com.coffee.atom.dto.approval;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApprovalPurchaseRequestDto {
    private Long id;
    
    @NotNull(message = "면장 ID는 필수입니다")
    @PositiveOrZero(message = "면장 ID는 0 이상이어야 합니다")
    private Long villageHeadId; // 면장 ID (1:1 관계)
    
    @NotNull(message = "차감액은 필수입니다")
    @PositiveOrZero(message = "차감액은 0 이상이어야 합니다")
    private Long deduction;
    
    @NotNull(message = "지급액은 필수입니다")
    @PositiveOrZero(message = "지급액은 0 이상이어야 합니다")
    private Long paymentAmount;
    
    @NotNull(message = "구매일자는 필수입니다")
    private LocalDate purchaseDate;
    
    @NotNull(message = "수량은 필수입니다")
    @PositiveOrZero(message = "수량은 0 이상이어야 합니다")
    private Long quantity;
    
    @NotNull(message = "총액은 필수입니다")
    @PositiveOrZero(message = "총액은 0 이상이어야 합니다")
    private Long totalPrice;
    
    @NotNull(message = "단가는 필수입니다")
    @PositiveOrZero(message = "단가는 0 이상이어야 합니다")
    private Long unitPrice;
    
    @Size(max = 500, message = "비고는 500자 이하여야 합니다")
    private String remarks;
}
