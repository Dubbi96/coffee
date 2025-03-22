package com.coffee.atom.domain.file;

import com.coffee.atom.domain.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FileEventLogRepository extends JpaRepository<FileEventLog,Long> {
    List<FileEventLog> findByAppUserOrderByCreatedAtDesc(AppUser appUser);
    List<FileEventLog> findByAppUserAndTypeOrderByCreatedAtDesc(AppUser appUser, FileEventLogType type);
    List<FileEventLog> findAllByOrderByCreatedAtDesc();
}
