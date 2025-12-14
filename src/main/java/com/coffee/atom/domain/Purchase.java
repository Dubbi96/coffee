package com.coffee.atom.domain;

import com.coffee.atom.domain.appuser.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "purchase")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private AppUser manager; // 담당자 (부 관리자)

    @ManyToOne
    @JoinColumn(name = "village_head_id", nullable = false)
    private AppUser villageHead; // 면장 (1:1 관계)

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "unit_price", nullable = false)
    private Long unitPrice;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "deduction", nullable = false)
    private Long deduction;

    @Column(name = "payment_amount", nullable = false)
    private Long paymentAmount;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "is_approved")
    private Boolean isApproved;

    public void approveInstance() {
        this.isApproved = true;
    }

    public void updateManager(AppUser manager) {
        this.manager = manager;
    }

    public void updateVillageHead(AppUser villageHead) {
        this.villageHead = villageHead;
    }

    public void updatePurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void updateQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public void updateUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void updateTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void updateDeduction(Long deduction) {
        this.deduction = deduction;
    }

    public void updatePaymentAmount(Long paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public void updateRemarks(String remarks) {
        this.remarks = remarks;
    }
}