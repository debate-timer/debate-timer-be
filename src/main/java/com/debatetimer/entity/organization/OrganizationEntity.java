package com.debatetimer.entity.organization;

import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.Organization;
import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "organization")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private String affiliation;

    @NotBlank
    private String iconPath;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Language language;

    public OrganizationEntity(String name, String affiliation, String iconPath, Language language) {
        this.name = name;
        this.affiliation = affiliation;
        this.iconPath = iconPath;
        this.language = language;
    }

    public Organization toDomain(List<OrganizationTemplate> templates) {
        return new Organization(this.id, this.name, this.affiliation, this.iconPath, templates);
    }
}
