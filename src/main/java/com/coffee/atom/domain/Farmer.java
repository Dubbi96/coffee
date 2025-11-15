package com.coffee.atom.domain;

import com.coffee.atom.domain.appuser.AppUser;
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
    @JoinColumn(name = "village_head_id")
    private AppUser villageHead;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "identification_photo_url")
    private String identificationPhotoUrl;

    @Column(name = "is_approved")
    private Boolean isApproved;

    public void approveInstance() {
        this.isApproved = true;
    }

    public void updateName(String name){
        this.name = name;
    }

    public void updateIdentificationPhotoUrl(String identificationPhotoUrl){
        this.identificationPhotoUrl = identificationPhotoUrl;
    }

    public void updateVillageHead(AppUser villageHead){
        this.villageHead = villageHead;
    }
}