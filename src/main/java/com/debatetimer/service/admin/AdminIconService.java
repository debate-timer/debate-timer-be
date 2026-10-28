package com.debatetimer.service.admin;

import com.debatetimer.client.storage.IconFile;
import com.debatetimer.domain.organization.OrganizationIcon;
import com.debatetimer.domainrepository.organization.OrganizationIconDomainRepository;
import com.debatetimer.dto.admin.IconResponse;
import com.debatetimer.dto.admin.IconResponses;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
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

    /**
     * 모든 아이콘을 원본 그대로 하나의 ZIP 으로 묶는다.
     */
    public byte[] zipAll() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (IconFile file : organizationIconDomainRepository.findAllFiles()) {
                zip.putNextEntry(new ZipEntry(file.fileName()));
                zip.write(file.content());
                zip.closeEntry();
            }
        } catch (IOException exception) {
            throw new DTServerErrorException(ServerErrorCode.ICON_STORAGE_ERROR);
        }
        return bytes.toByteArray();
    }
}
