package com.debatetimer.service.admin;

import com.debatetimer.client.storage.IconFile;
import com.debatetimer.domain.organization.OrganizationIcon;
import com.debatetimer.domainrepository.organization.OrganizationIconDomainRepository;
import com.debatetimer.dto.admin.IconResponse;
import com.debatetimer.dto.admin.IconResponses;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * 모든 아이콘을 원본 그대로 하나의 ZIP 으로 묶어 출력 스트림에 쓴다.
     * 아이콘을 하나씩 읽어 바로 쓰므로 전체 아이콘을 메모리에 올리지 않는다.
     */
    public void writeZip(OutputStream output) throws IOException {
        // 응답 스트림은 서블릿 컨테이너가 닫으므로 ZIP 은 마무리만 한다
        ZipOutputStream zip = new ZipOutputStream(output);
        for (IconFile file : organizationIconDomainRepository.findAllFiles()) {
            zip.putNextEntry(new ZipEntry(file.fileName()));
            try (InputStream content = file.open()) {
                content.transferTo(zip);
            }
            zip.closeEntry();
        }
        zip.finish();
    }
}
