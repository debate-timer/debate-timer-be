package com.debatetimer.entity.organization;


import com.debatetimer.domain.organization.Language;
import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "organization_template")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationTemplateEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @NotBlank
    private String name;

    @NotBlank
    @Column(length = 8191)
    private String data;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Language language;

    public OrganizationTemplateEntity(OrganizationEntity organization, String name, String data, Language language) {
        this.organization = organization;
        this.name = name;
        this.data = data;
        this.language = language;
    }

    // 템플릿은 단체와 같은 언어로 조회되므로 단체의 언어를 그대로 따른다
    public OrganizationTemplateEntity(OrganizationEntity organization, OrganizationTemplate template) {
        this(organization, template.getName(), template.getData(), organization.getLanguage());
    }

    public OrganizationTemplate toDomain() {
        return new OrganizationTemplate(this.id, this.name, this.data);
    }

    public Long getOrganizationId() {
        return this.organization.getId();
    }
}
