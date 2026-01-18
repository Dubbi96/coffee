package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.area.Area;
import com.coffee.atom.dto.appuser.VillageHeadResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUserId(String userId);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findAppUserByIsApprovedAndId(Boolean isApproved, Long id);

    // 역할과 지역 기반 조회
    List<AppUser> findByRoleAndArea(Role role, Area area);

    // 역할과 섹션 기반 조회
    List<AppUser> findByRoleAndSection(Role role, com.coffee.atom.domain.area.Section section);

    // 면장 목록 조회 (농부 수 포함) - 전체
    @Query("SELECT new com.coffee.atom.dto.appuser.VillageHeadResponseDto( " +
           " u.id, u.userId, u.username, s.sectionName, COUNT(DISTINCT f.id), " +
           " area.id, area.longitude, area.latitude, area.areaName, " +
           " s.id, s.longitude, s.latitude ) " +
           "FROM AppUser u " +
           "LEFT JOIN u.section s " +
           "LEFT JOIN s.area area " +
           "LEFT JOIN Farmer f ON f.villageHead.id = u.id AND f.isApproved = true " +
           "WHERE u.role = com.coffee.atom.domain.appuser.Role.VILLAGE_HEAD " +
           "AND u.isApproved = true " +
           "AND s.isApproved = true " +
           "GROUP BY u.id, u.userId, u.username, s.sectionName, area.id, area.longitude, area.latitude, area.areaName, s.id, s.longitude, s.latitude")
    List<VillageHeadResponseDto> findAllVillageHeadsWithFarmerCountForAdmin();

    // 면장 목록 조회 (농부 수 포함) - 지역별
    @Query("SELECT new com.coffee.atom.dto.appuser.VillageHeadResponseDto( " +
           " u.id, u.userId, u.username, s.sectionName, COUNT(DISTINCT f.id), " +
           " area.id, area.longitude, area.latitude, area.areaName, " +
           " s.id, s.longitude, s.latitude ) " +
           "FROM AppUser u " +
           "LEFT JOIN u.section s " +
           "LEFT JOIN s.area area " +
           "LEFT JOIN Farmer f ON f.villageHead.id = u.id AND f.isApproved = true " +
           "WHERE area.id = :areaId " +
           "AND u.role = com.coffee.atom.domain.appuser.Role.VILLAGE_HEAD " +
           "AND u.isApproved = true " +
           "AND s.isApproved = true " +
           "GROUP BY u.id, u.userId, u.username, s.sectionName, area.id, area.longitude, area.latitude, area.areaName, s.id, s.longitude, s.latitude")
    List<VillageHeadResponseDto> findAllVillageHeadsWithFarmerCountByAreaId(@Param("areaId") Long areaId);

    // 부관리자 목록 조회 (지역 포함)
    @Query("SELECT u FROM AppUser u JOIN FETCH u.area WHERE u.role IN :roles")
    List<AppUser> findAllViceAdminsWithArea(@Param("roles") List<Role> roles);

    // 부관리자 상세 조회 (지역 포함)
    @Query("SELECT u FROM AppUser u JOIN FETCH u.area WHERE u.id = :id AND u.role IN :roles")
    Optional<AppUser> findViceAdminByIdWithArea(@Param("id") Long id, @Param("roles") List<Role> roles);

    // 지역 ID로 부관리자 사용자 ID 목록 조회
    @Query("SELECT u.id FROM AppUser u WHERE u.area.id = :areaId AND u.role IN :roles")
    List<Long> findViceAdminUserIdsByAreaId(@Param("areaId") Long areaId, @Param("roles") List<Role> roles);

    // 사용자 ID로 지역 ID 조회
    @Query("SELECT u.area.id FROM AppUser u WHERE u.id = :appUserId AND u.role IN :roles")
    Optional<Long> findAreaIdByAppUserId(@Param("appUserId") Long appUserId, @Param("roles") List<Role> roles);

    // 지역과 역할로 부관리자 조회
    List<AppUser> findByAreaAndRole(Area area, Role role);

    // 내 정보 조회용 (area와 section을 함께 fetch)
    @Query("SELECT u FROM AppUser u LEFT JOIN FETCH u.area LEFT JOIN FETCH u.section LEFT JOIN FETCH u.section.area WHERE u.id = :id")
    Optional<AppUser> findByIdWithAreaAndSection(@Param("id") Long id);
}
