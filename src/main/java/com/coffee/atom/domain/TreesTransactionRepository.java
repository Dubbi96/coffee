package com.coffee.atom.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TreesTransactionRepository extends JpaRepository<TreesTransaction, Long> {
    // In TreesTransactionRepository
    @Query("SELECT t FROM TreesTransaction t " +
           "WHERE t.farmer.id = :farmerId AND t.isApproved = true")
    List<TreesTransaction> findAllApprovedByFarmerId(@Param("farmerId") Long farmerId);

    List<TreesTransaction> findByIsApprovedTrueOrderByReceivedDateDesc();

    List<TreesTransaction> findByIsApprovedTrueAndFarmer_VillageHead_Section_Area_IdOrderByReceivedDateDesc(Long areaId);

    List<TreesTransaction> findByIsApprovedTrueAndFarmer_VillageHead_Section_IdOrderByReceivedDateDesc(Long sectionId);
}
