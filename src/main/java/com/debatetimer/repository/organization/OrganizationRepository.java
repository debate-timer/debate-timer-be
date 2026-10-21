package com.debatetimer.repository.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface OrganizationRepository extends Repository<OrganizationEntity, Long> {

    List<OrganizationEntity> findAllByLanguage(Language language);

    List<OrganizationEntity> findAllByOrderByIdAsc();

    Optional<OrganizationEntity> findById(long id);

    default OrganizationEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.ORGANIZATION_NOT_FOUND));
    }

    OrganizationEntity save(OrganizationEntity organizationEntity);
}
