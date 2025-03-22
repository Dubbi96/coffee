package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.Section;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vice_admin_detail")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViceAdminDetail {

    @Id
    private Long id;

    @OneToOne
    @MapsId // 부모 엔티티(AppUser)의 PK를 이 엔티티의 PK로 사용
    @JoinColumn(name = "id") // app_user_id와 동일한 역할
    private AppUser appUser;

    @OneToMany(mappedBy = "viceAdminDetail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ViceAdminSection> viceAdminSections = new ArrayList<>();

    @Column(name = "id_card_url", nullable = false)
    private String idCardUrl;

    public ViceAdminDetail updateIdCardUrl(String idCardUrl) {
        this.idCardUrl = idCardUrl;
        return this;
    }
}
