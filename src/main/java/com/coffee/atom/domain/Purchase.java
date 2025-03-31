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

    @Column(name = "is_approved")
    private Boolean isApproved;

    public void approveInstance() {
        this.isApproved = true;
    }
}