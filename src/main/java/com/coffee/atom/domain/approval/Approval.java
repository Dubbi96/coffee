package com.coffee.atom.domain.approval;

import com.coffee.atom.domain.appuser.AppUser;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "method", nullable = false)
    private Method method;

    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Type(JsonType.class)
    @Column(name = "requested_data", columnDefinition = "jsonb", nullable = false)
    private String requestedData;

    @OneToMany(mappedBy = "approval", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequestedInstance> requestedInstance = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}