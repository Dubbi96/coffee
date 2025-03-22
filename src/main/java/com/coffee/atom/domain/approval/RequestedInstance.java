package com.coffee.atom.domain.approval;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requested_instance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestedInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @Column(name = "instance_id")
    private Long instanceId;

    @ManyToOne
    @JoinColumn(name = "approval", nullable = false)
    private Approval approval;
}
