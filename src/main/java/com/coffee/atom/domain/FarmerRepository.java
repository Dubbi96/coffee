package com.coffee.atom.domain;

import com.coffee.atom.dto.FarmersResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    @Query("SELECT new com.coffee.atom.dto.FarmersResponseDto( " +
            "f.id, f.name, vh.username, s.sectionName) " +
            "FROM Farmer f " +
            "JOIN f.villageHead vh " +
            "JOIN vh.section s " +
            "WHERE f.isApproved = true " +
            "AND vh.isApproved = true " +
            "AND s.isApproved = true")
    List<FarmersResponseDto> findAllApprovedFarmersWithVillageHeadAndSection();

    @Query("SELECT new com.coffee.atom.dto.FarmersResponseDto( " +
           "f.id, f.name, vh.username, s.sectionName) " +
           "FROM Farmer f " +
           "JOIN f.villageHead vh " +
           "JOIN vh.section s " +
           "JOIN s.area a " +
           "WHERE f.isApproved = true " +
           "AND vh.isApproved = true " +
           "AND s.isApproved = true " +
           "AND a.id = :areaId")
    List<FarmersResponseDto> findAllByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT new com.coffee.atom.dto.FarmersResponseDto( " +
           "f.id, f.name, vh.username, s.sectionName) " +
           "FROM Farmer f " +
           "JOIN f.villageHead vh " +
           "JOIN vh.section s " +
           "WHERE f.isApproved = true " +
           "AND vh.isApproved = true " +
           "AND s.isApproved = true " +
           "AND vh.id = :villageHeadId")
    List<FarmersResponseDto> findAllByVillageHeadId(@Param("villageHeadId") Long villageHeadId);
}
