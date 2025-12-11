package com.debatetimer.domainrepository.organization;

import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.entity.organization.OrganizationTemplateEntity;
import com.debatetimer.repository.organization.OrganizationRepository;
import com.debatetimer.repository.organization.OrganizationTemplateRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrganizationDomainRepository {

    private final OrganizationRepository organizationRepository;
    private final OrganizationTemplateRepository organizationTemplateRepository;

    public List<Organization> findAll() {
        Map<Long, List<OrganizationTemplate>> idToTemplatesEntity = organizationTemplateRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        OrganizationTemplateEntity::getOrganizationId,
                        Collectors.mapping(OrganizationTemplateEntity::toDomain, Collectors.toList())
                ));

        return organizationRepository.findAll()
                .stream()
                .map(entity -> entity.toDomain(
                        idToTemplatesEntity.getOrDefault(entity.getId(), Collections.emptyList())
                )).toList();
    }
}
