package com.debatetimer.repository.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.OrganizationCategory;
import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface OrganizationRepository extends Repository<OrganizationEntity, Long> {

    List<OrganizationEntity> findAllByLanguage(Language language);

    List<OrganizationEntity> findAllByOrderByIdAsc();

    Optional<OrganizationEntity> findById(long id);

    default OrganizationEntity getById(long id) {
        return findById(id)
                .orElseThrow(() -> new DTClientErrorException(ClientErrorCode.ORGANIZATION_NOT_FOUND));
    }

    OrganizationEntity save(OrganizationEntity organizationEntity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrganizationEntity o
            SET o.name = :name, o.affiliation = :affiliation, o.iconPath = :iconPath,
                o.language = :language, o.category = :category, o.modifiedAt = :modifiedAt
            WHERE o.id = :id
            """)
    int update(@Param("id") long id,
               @Param("name") String name,
               @Param("affiliation") String affiliation,
               @Param("iconPath") String iconPath,
               @Param("language") Language language,
               @Param("category") OrganizationCategory category,
               @Param("modifiedAt") LocalDateTime modifiedAt);
}
