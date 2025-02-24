package com.coffee.atom.domain.appuser;

import com.coffee.atom.domain.sensor.SensorGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppUserSensorGroupRepository extends JpaRepository<AppUserSensorGroup, Long> {
    List<AppUserSensorGroup> findByAppUser(AppUser appUser);
    boolean existsByAppUserAndSensorGroup(AppUser appUser, SensorGroup sensorGroup);
}
