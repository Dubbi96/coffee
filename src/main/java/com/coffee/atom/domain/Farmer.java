package com.coffee.atom.domain;

import com.coffee.atom.domain.appuser.VillageHeadDetail;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "farmer")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "village_head_id", nullable = false)
    private VillageHeadDetail villageHead;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "identification_photo_url")
    private String identificationPhotoUrl;

    @Column(name = "is_approved")
    private Boolean isApproved;

    public void approveInstance() {
        this.isApproved = true;
    }
}