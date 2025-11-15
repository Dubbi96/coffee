package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.Section;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_user_id")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "salt", nullable = false, length = 50)
    private String salt;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at", updatable = false, nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @Column(name = "is_approved")
    private Boolean isApproved;

    // 부관리자용 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @Column(name = "id_card_url")
    private String idCardUrl;

    @Column(name = "area_locked")
    private Boolean areaLocked;

    // 면장용 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
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

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String encodedPassword, String salt) {
        this.password = encodedPassword;
        this.salt = salt;
    }

    public void updateUserName(String userName) {
        this.username = userName;
    }

    public void updateUserId(String userId) {
        this.userId = userId;
    }

    public void approveInstance() {
        this.isApproved = true;
    }

    // 부관리자 필드 업데이트 메서드
    public void updateArea(Area area) {
        this.area = area;
    }

    public void updateIdCardUrl(String idCardUrl) {
        this.idCardUrl = idCardUrl;
    }

    public void updateAreaLocked(Boolean areaLocked) {
        this.areaLocked = areaLocked;
    }

    // 면장 필드 업데이트 메서드
    public void updateSection(Section section) {
        this.section = section;
    }

    public void updateIdentificationPhotoUrl(String identificationPhotoUrl) {
        this.identificationPhotoUrl = identificationPhotoUrl;
    }

    public void updateBankName(String bankName) {
        this.bankName = bankName;
    }

    public void updateAccountInfo(String accountInfo) {
        this.accountInfo = accountInfo;
    }

    public void updateContractUrl(String contractUrl) {
        this.contractUrl = contractUrl;
    }

    public void updateBankbookUrl(String bankbookUrl) {
        this.bankbookUrl = bankbookUrl;
    }

}