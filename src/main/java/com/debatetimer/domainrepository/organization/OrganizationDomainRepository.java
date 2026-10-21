package com.debatetimer.domainrepository.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.entity.organization.OrganizationTemplateEntity;
import com.debatetimer.repository.organization.OrganizationRepository;
import com.debatetimer.repository.organization.OrganizationTemplateRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class OrganizationDomainRepository {

    private final OrganizationRepository organizationRepository;
    private final OrganizationTemplateRepository organizationTemplateRepository;

    public List<Organization> findAllByLanguage(Language language) {
        Map<Long, List<OrganizationTemplate>> idToTemplatesEntity = organizationTemplateRepository.findAllByLanguage(language)
                .stream()
                .collect(Collectors.groupingBy(
                        OrganizationTemplateEntity::getOrganizationId,
                        Collectors.mapping(OrganizationTemplateEntity::toDomain, Collectors.toList())
                ));

        return organizationRepository.findAllByLanguage(language)
                .stream()
                .map(entity -> entity.toDomain(
                        idToTemplatesEntity.getOrDefault(entity.getId(), Collections.emptyList())
                )).toList();
    }

    @Transactional(readOnly = true)
    public List<Organization> findAll() {
        Map<Long, List<OrganizationTemplate>> idToTemplates = organizationTemplateRepository.findAllByOrderByIdAsc()
                .stream()
                .collect(Collectors.groupingBy(
                        OrganizationTemplateEntity::getOrganizationId,
                        Collectors.mapping(OrganizationTemplateEntity::toDomain, Collectors.toList())
                ));

        return organizationRepository.findAllByOrderByIdAsc()
                .stream()
                .map(entity -> entity.toDomain(idToTemplates.getOrDefault(entity.getId(), Collections.emptyList())))
                .toList();
    }

    @Transactional
    public Organization create(Organization organization) {
        return organizationRepository.save(new OrganizationEntity(organization))
                .toDomain(Collections.emptyList());
    }

    @Transactional
    public OrganizationTemplate createTemplate(long organizationId, OrganizationTemplate template) {
        OrganizationEntity organization = organizationRepository.getById(organizationId);
        return organizationTemplateRepository.save(new OrganizationTemplateEntity(organization, template))
                .toDomain();
    }
}
