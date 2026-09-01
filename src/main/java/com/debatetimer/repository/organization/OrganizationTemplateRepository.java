package com.debatetimer.repository.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.entity.organization.OrganizationTemplateEntity;
import java.util.List;
import org.springframework.data.repository.Repository;

public interface OrganizationTemplateRepository extends Repository<OrganizationTemplateEntity, Long> {

    List<OrganizationTemplateEntity> findAllByLanguage(Language language);

    OrganizationTemplateEntity save(OrganizationTemplateEntity entity);
}
