package com.coffee.atom.domain.appuser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ViceAdminSectionRepository extends JpaRepository<ViceAdminSection, Long> {
     @Query("SELECT vs.section.id FROM ViceAdminSection vs WHERE vs.viceAdminDetail.id = :viceAdminId")
     List<Long> findSectionIdsByViceAdminId(@Param("viceAdminId") Long viceAdminId);
}
