package com.coffee.atom.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long>, JpaSpecificationExecutor<Purchase> {
    // ADMIN용: 전체 조회
    List<Purchase> findByIsApprovedTrueOrderByPurchaseDateDesc();

    // VICE_ADMIN용: 본인 manager일 경우만
    List<Purchase> findByIsApprovedTrueAndManager_IdOrderByPurchaseDateDesc(Long managerId);

    // VILLAGE_HEAD용: 본인과 1:1 관계인 Purchase만 조회
    List<Purchase> findByIsApprovedTrueAndVillageHead_IdOrderByPurchaseDateDesc(Long villageHeadId);
}
