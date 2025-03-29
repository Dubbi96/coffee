package com.coffee.atom.domain.appuser;

import com.coffee.atom.dto.appuser.VillageHeadResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VillageHeadDetailRepository extends JpaRepository<VillageHeadDetail, Long> {
    @Query("SELECT new com.coffee.atom.dto.appuser.VillageHeadResponseDto( " +
           " v.id, v.appUser.userId, v.appUser.username, s.sectionName, COUNT(DISTINCT f.id) ) " +
           "FROM VillageHeadDetail v " +
           "LEFT JOIN v.appUser a " +
           "LEFT JOIN v.section s " +
           "LEFT JOIN Farmer f ON f.villageHead.id = v.id AND f.isApproved = true " +
           "WHERE v.isApproved = true " +
           "AND a.isApproved = true " +
           "AND s.isApproved = true " +
           "GROUP BY v.id, v.appUser.userId, v.appUser.username, s.sectionName")
    List<VillageHeadResponseDto> findAllWithFarmerCountForAdmin();

   @Query("SELECT new com.coffee.atom.dto.appuser.VillageHeadResponseDto( " +
           " v.id, a.userId, a.username, s.sectionName, COUNT(DISTINCT f.id) ) " +
           "FROM VillageHeadDetail v " +
           "LEFT JOIN v.appUser a " +
           "LEFT JOIN v.section s " +
           "LEFT JOIN s.area area " +
           "LEFT JOIN Farmer f ON f.villageHead.id = v.id AND f.isApproved = true " +
           "WHERE area.id = :areaId " +
           "AND v.isApproved = true " +
           "AND a.isApproved = true " +
           "AND s.isApproved = true " +
           "GROUP BY v.id, a.userId, a.username, s.sectionName")
   List<VillageHeadResponseDto> findAllWithFarmerCountByAreaId(@Param("areaId") Long areaId);

   Optional<VillageHeadDetail> findVillageHeadDetailByIsApprovedAndId(Boolean isApproved, Long id);
}
