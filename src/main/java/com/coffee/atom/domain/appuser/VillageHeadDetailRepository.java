package com.coffee.atom.domain.appuser;

import com.coffee.atom.dto.appuser.VillageHeadResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VillageHeadDetailRepository extends JpaRepository<VillageHeadDetail, Long> {
    @Query("SELECT new com.coffee.atom.dto.appuser.VillageHeadResponseDto( " +
           " v.id, v.appUser.userId, v.appUser.username, s.sectionName, COUNT(f.id) ) " +
           "FROM VillageHeadDetail v " +
           "LEFT JOIN v.appUser a " +
           "LEFT JOIN v.section s " +
           "LEFT JOIN Farmer f ON f.villageHead.id = v.id " +
           "GROUP BY v.id, v.appUser.userId, v.appUser.username, s.sectionName")
    List<VillageHeadResponseDto> findAllWithFarmerCountForAdmin();

   @Query("SELECT new com.coffee.atom.dto.appuser.VillageHeadResponseDto( " +
           " v.id, v.appUser.userId, v.appUser.username, s.sectionName, COUNT(f.id) ) " +
           "FROM VillageHeadDetail v " +
           "LEFT JOIN v.appUser a " +
           "LEFT JOIN v.section s " +
           "LEFT JOIN Farmer f ON f.villageHead.id = v.id " +
           "WHERE s.id IN :sectionIds " +  // Vice Admin이 관리하는 모든 Section 조회
           "GROUP BY v.id, v.appUser.userId, v.appUser.username, s.sectionName")
    List<VillageHeadResponseDto> findAllWithFarmerCountForViceAdmin(@Param("sectionIds") List<Long> sectionIds);
}
