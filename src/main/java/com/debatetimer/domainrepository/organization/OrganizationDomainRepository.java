package com.debatetimer.domainrepository.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.entity.organization.OrganizationTemplateEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.repository.organization.OrganizationRepository;
import com.debatetimer.repository.organization.OrganizationTemplateJdbcRepository;
import com.debatetimer.repository.organization.OrganizationTemplateRepository;
import java.time.LocalDateTime;
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
    private final OrganizationTemplateJdbcRepository organizationTemplateJdbcRepository;

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

    // 단체를 먼저 갱신해 템플릿 추가(INSERT ... SELECT)와 같은 단체 행에서 순서가 정해지게 한다
    @Transactional
    public Organization update(long organizationId, Organization organization) {
        LocalDateTime now = LocalDateTime.now();
        int updated = organizationRepository.update(organizationId, organization.getName(),
                organization.getAffiliation(), organization.getIconPath(), organization.getLanguage(),
                organization.getCategory(), now);
        if (updated == 0) {
            throw new DTClientErrorException(ClientErrorCode.ORGANIZATION_NOT_FOUND);
        }

        // 템플릿은 단체와 같은 언어로 조회되므로 단체의 언어가 바뀌면 함께 바꾼다
        organizationTemplateRepository.updateLanguageByOrganizationId(
                organizationId, organization.getLanguage(), now);

        List<OrganizationTemplate> templates =
                organizationTemplateRepository.findAllByOrganization_IdOrderByIdAsc(organizationId)
                        .stream()
                        .map(OrganizationTemplateEntity::toDomain)
                        .toList();
        return organizationRepository.getById(organizationId).toDomain(templates);
    }

    @Transactional
    public OrganizationTemplate createTemplate(long organizationId, OrganizationTemplate template) {
        long templateId = organizationTemplateJdbcRepository.insertWithOrganizationLanguage(
                        organizationId, template.getName(), template.getData())
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.ORGANIZATION_NOT_FOUND));
        return new OrganizationTemplate(templateId, template.getName(), template.getData());
    }
}
