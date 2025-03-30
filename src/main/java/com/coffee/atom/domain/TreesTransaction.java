package com.coffee.atom.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trees_transaction")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @Column(name = "species", nullable = false)
    private String species;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "is_approved")
    private Boolean isApproved;
}