package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.Section;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vice_admin_detail")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ViceAdminDetail {

    @Id
    private Long id;

    @OneToOne
    @MapsId // 부모 엔티티(AppUser)의 PK를 이 엔티티의 PK로 사용
    @JoinColumn(name = "id") // app_user_id와 동일한 역할
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "id_card_url", nullable = false)
    private String idCardUrl;

    @Column(name = "name", nullable = false)
    private String name;
}
