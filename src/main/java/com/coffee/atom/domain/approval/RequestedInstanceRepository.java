package com.coffee.atom.domain.approval;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestedInstanceRepository extends JpaRepository<RequestedInstance, Long> {
}
