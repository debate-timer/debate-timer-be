package com.debatetimer.repository.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.entity.organization.OrganizationTemplateEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface OrganizationTemplateRepository extends Repository<OrganizationTemplateEntity, Long> {

    List<OrganizationTemplateEntity> findAllByLanguage(Language language);

    List<OrganizationTemplateEntity> findAllByOrderByIdAsc();

    List<OrganizationTemplateEntity> findAllByOrganization_IdOrderByIdAsc(long organizationId);

    OrganizationTemplateEntity save(OrganizationTemplateEntity entity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrganizationTemplateEntity t
            SET t.language = :language, t.modifiedAt = :modifiedAt
            WHERE t.organization.id = :organizationId
            """)
    void updateLanguageByOrganizationId(@Param("organizationId") long organizationId,
                                        @Param("language") Language language,
                                        @Param("modifiedAt") LocalDateTime modifiedAt);
}
