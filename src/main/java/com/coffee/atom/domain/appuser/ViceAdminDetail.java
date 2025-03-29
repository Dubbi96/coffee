package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.area.Area;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    private Area area;

    @Column(name = "id_card_url")
    private String idCardUrl;

    public ViceAdminDetail updateIdCardUrl(String idCardUrl) {
        this.idCardUrl = idCardUrl;
        return this;
    }
}
