package com.debatetimer.entity.organization;


import com.debatetimer.domain.organization.OrganizationTemplate;
import com.debatetimer.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    public OrganizationTemplateEntity(OrganizationEntity organization, String name, String data) {
        this.organization = organization;
        this.name = name;
        this.data = data;
    }

    public OrganizationTemplate toDomain() {
        return new OrganizationTemplate(this.id, this.name, this.data);
    }

    public Long getOrganizationId() {
        return this.organization.getId();
    }
}
