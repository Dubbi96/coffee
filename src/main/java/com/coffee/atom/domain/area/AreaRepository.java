package com.coffee.atom.domain.area;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AreaRepository extends JpaRepository<Area, Long> {
    @Query("SELECT DISTINCT a FROM Area a LEFT JOIN FETCH a.sections")
    List<Area> findAreaWithSections();
}
