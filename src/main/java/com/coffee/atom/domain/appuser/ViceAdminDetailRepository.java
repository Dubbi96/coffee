package com.coffee.atom.domain.appuser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViceAdminDetailRepository extends JpaRepository<ViceAdminDetail, Long> {
    Optional<ViceAdminDetail> findByAppUserId(Long appUserId);

}
