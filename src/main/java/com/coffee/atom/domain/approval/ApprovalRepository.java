package com.coffee.atom.domain.approval;

import com.coffee.atom.domain.appuser.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval,Long>, JpaSpecificationExecutor<Approval> {
    List<Approval> findByApprover(AppUser approver);
    List<Approval> findByRequester(AppUser requester);
}
