package com.debatetimer.service.admin;

import com.debatetimer.domain.organization.OrganizationIcon;
import com.debatetimer.domainrepository.organization.OrganizationIconDomainRepository;
import com.debatetimer.dto.admin.IconResponse;
import com.debatetimer.dto.admin.IconResponses;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminIconService {

    private final OrganizationIconDomainRepository organizationIconDomainRepository;

    public IconResponses findAll() {
        return new IconResponses(Stream.concat(
                organizationIconDomainRepository.findBundledPaths().stream()
                        .map(path -> new IconResponse(path, false)),
                organizationIconDomainRepository.findUploadedPaths().stream()
                        .map(path -> new IconResponse(path, true))
        ).toList());
    }

    public IconResponse upload(String name, byte[] content) {
        String path = organizationIconDomainRepository.save(new OrganizationIcon(name, content));
        return new IconResponse(path, true);
    }
}
