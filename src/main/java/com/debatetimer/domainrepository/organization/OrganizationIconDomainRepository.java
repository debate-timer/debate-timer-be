package com.debatetimer.domainrepository.organization;

import com.debatetimer.client.storage.IconFile;
import com.debatetimer.client.storage.IconFileStorage;
import com.debatetimer.domain.organization.OrganizationIcon;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrganizationIconDomainRepository {

    private final IconFileStorage iconFileStorage;

    public List<String> findBundledPaths() {
        return toPaths(iconFileStorage.findBundledFileNames());
    }

    public List<String> findUploadedPaths() {
        return toPaths(iconFileStorage.findUploadedFileNames());
    }

    public List<IconFile> findAllFiles() {
        return iconFileStorage.findAll();
    }

    private List<String> toPaths(List<String> fileNames) {
        return fileNames.stream()
                .map(fileName -> OrganizationIcon.PATH_PREFIX + fileName)
                .toList();
    }

    public String save(OrganizationIcon icon) {
        if (iconFileStorage.exists(icon.getFileName())
                || !iconFileStorage.saveIfAbsent(icon.getFileName(), icon.getContent())) {
            throw new DTClientErrorException(ClientErrorCode.ICON_ALREADY_EXISTS);
        }
        return icon.getPath();
    }
}
