package com.coffee.atom.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "trees_transaction")
@Getter
@Setter
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
    private LocalDateTime receivedDate;

    @Column(name = "quantity", nullable = false)
    private Long quantity;
}