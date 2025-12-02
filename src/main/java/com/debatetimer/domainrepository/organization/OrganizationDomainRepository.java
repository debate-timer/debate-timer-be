package com.debatetimer.domainrepository.organization;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.entity.organization.OrganizationTemplateEntity;
import com.debatetimer.repository.organization.OrganizationRepository;
import com.debatetimer.repository.organization.OrganizationTemplateRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrganizationDomainRepository {

    private final OrganizationRepository organizationRepository;
    private final OrganizationTemplateRepository organizationTemplateDomainRepository;

    public List<Organization> findAll() {
        Map<Long, List<OrganizationTemplate>> idToTemplatesEntity = organizationTemplateDomainRepository.findAll()
                .stream()
                .collect(groupingBy(
                        OrganizationTemplateEntity::getOrganizationId,
                        mapping(OrganizationTemplateEntity::toDomain, toList()))
                );

        return organizationRepository.findAll()
                .stream()
                .map(entity -> entity.toDomain(idToTemplatesEntity.get(entity.getId())))
                .toList();
    }
}
