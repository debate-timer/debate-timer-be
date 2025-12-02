package com.debatetimer.repository.organization;

import com.debatetimer.entity.organization.OrganizationEntity;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface OrganizationRepository extends Repository<OrganizationEntity, Long> {

    List<OrganizationEntity> findAll();

    OrganizationEntity save(OrganizationEntity organizationEntity);
}
