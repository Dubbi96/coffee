package com.coffee.atom.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    // ADMIN용: 전체 조회
    List<Purchase> findByIsApprovedTrueOrderByPurchaseDateDesc();

    // VICE_ADMIN_HEAD_OFFICER용: 본인 manager일 경우만
    List<Purchase> findByIsApprovedTrueAndManager_IdOrderByPurchaseDateDesc(Long managerId);
}
