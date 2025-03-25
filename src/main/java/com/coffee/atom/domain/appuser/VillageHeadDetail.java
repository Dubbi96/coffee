package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.area.Section;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "village_head_detail")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillageHeadDetail {

    @Id
    private Long id;

    @OneToOne
    @MapsId // 부모 엔티티(AppUser)의 PK를 이 엔티티의 PK로 사용
    @JoinColumn(name = "id") // app_user_id와 동일한 역할
    private AppUser appUser;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "identification_photo_url")
    private String identificationPhotoUrl;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_info")
    private String accountInfo;

    @Column(name = "contract_url")
    private String contractUrl;

    @Column(name = "bankbook_url")
    private String bankbookUrl;

    @Column(name = "is_approved")
    private Boolean isApproved;
}
