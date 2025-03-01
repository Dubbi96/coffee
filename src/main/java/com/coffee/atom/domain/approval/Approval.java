package com.coffee.atom.domain.approval;

import com.coffee.atom.domain.appuser.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approval {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "approver_id")
    private AppUser approver;

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private AppUser requester;

    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "requested_data", columnDefinition = "jsonb", nullable = false)
    private String requestedData;

    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}