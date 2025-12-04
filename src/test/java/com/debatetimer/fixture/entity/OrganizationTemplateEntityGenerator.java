package com.debatetimer.fixture.entity;

import com.debatetimer.entity.organization.OrganizationEntity;
import com.debatetimer.entity.organization.OrganizationTemplateEntity;
import com.debatetimer.repository.organization.OrganizationTemplateRepository;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTemplateEntityGenerator {

    private static final String DEFAULT_TEMPLATE_CONTENT = "eJyrVspMUbIytjDXUcrMS8tXsqpWykvMTVWyUjJWKCtWMFZ427b1TXPj27YFrxcueN3T8HZWj8LbGVPfdM9V0lEqqSwAqXQODQ7x9%2FWMcgUKJaan5qUkAgWB7IKi%2FOKQ1MRcP4iBbzasedOyESienJ%2BHLP56wwygwUDx8sSivMy8dKfUnBwlq7TEnOJUHaW0zLzM4gwkoVqgtYlJOUCN0dVKxSWJeckgMwKC%2FIOBJhQXpKYmZ4RAnPVmXivQzUDRpPwKqJCff5Cvow%2FI5Zkgqw2NDICyYLPzSnNyIMIBqUUgx6EJBRekJmYDHQcTLgbxU4sg3FodJKc4%2B%2FsNFqf4uYaGBIEtQXPNhDdzFkCiFMVNIZ6%2BrvFOjsGuLnB3QazA6TBzkLMx3AX2DIkhtHXOm0WtCq83zHkzbQfdAwpr8qG7i2JrAbdLRw0%3D";

    private final OrganizationTemplateRepository organizationTemplateRepository;

    public OrganizationTemplateEntityGenerator(OrganizationTemplateRepository organizationTemplateRepository) {
        this.organizationTemplateRepository = organizationTemplateRepository;
    }

    public OrganizationTemplateEntity generate(OrganizationEntity organization, String name) {
        OrganizationTemplateEntity template =
                new OrganizationTemplateEntity(organization, name, DEFAULT_TEMPLATE_CONTENT);
        return organizationTemplateRepository.save(template);
    }
}
