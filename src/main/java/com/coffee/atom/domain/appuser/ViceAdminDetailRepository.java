package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.area.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ViceAdminDetailRepository extends JpaRepository<ViceAdminDetail, Long> {
    Optional<ViceAdminDetail> findByAppUserId(Long appUserId);

    @Query("SELECT v.area.id " +
       "FROM ViceAdminDetail v " +
       "WHERE v.appUser.id = :appUserId")
    Optional<Long> findAreaIdByAppUser_Id(Long appUserId);

    @Query("SELECT v.appUser.id " +
       "FROM ViceAdminDetail v " +
       "WHERE v.area.id = :areaId")
    List<Long> findViceAdminUserIdsByAreaId(@Param("areaId") Long areaId);

    @Query("SELECT v FROM ViceAdminDetail v JOIN FETCH v.appUser a JOIN FETCH v.area")
    List<ViceAdminDetail> findAllWithAppUserAndArea();

    @Query("SELECT v FROM ViceAdminDetail v JOIN FETCH v.appUser a JOIN FETCH v.area WHERE v.id = :id")
    Optional<ViceAdminDetail> findByIdWithAppUserAndArea(@Param("id") Long id);

    List<ViceAdminDetail> findByAreaAndAppUser_Role(Area area, Role role);
}
