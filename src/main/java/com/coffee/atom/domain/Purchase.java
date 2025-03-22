package com.coffee.atom.domain;

import com.coffee.atom.domain.appuser.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "purchase")
@Getter
@Setter
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
    private LocalDateTime purchaseDate;

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
}