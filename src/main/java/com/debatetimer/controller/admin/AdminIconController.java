package com.debatetimer.controller.admin;

import com.debatetimer.dto.admin.IconResponse;
import com.debatetimer.dto.admin.IconResponses;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import com.debatetimer.service.admin.AdminIconService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AdminIconController {

    private final AdminAuthorizer adminAuthorizer;
    private final AdminIconService adminIconService;

    @GetMapping("/api/admin/icons")
    @ResponseStatus(HttpStatus.OK)
    public IconResponses getIcons(
            @RequestHeader(name = AdminSharingController.ADMIN_PASSWORD_HEADER, required = false) String password
    ) {
        adminAuthorizer.authorize(password);
        return adminIconService.findAll();
    }

    @PostMapping(value = "/api/admin/icons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public IconResponse uploadIcon(
            @RequestHeader(name = AdminSharingController.ADMIN_PASSWORD_HEADER, required = false) String password,
            @RequestPart("file") MultipartFile file,
            @RequestParam(name = "name", required = false) String name
    ) {
        adminAuthorizer.authorize(password);
        String fileName = (name == null || name.isBlank()) ? file.getOriginalFilename() : name;
        return adminIconService.upload(fileName, readBytes(file));
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new DTServerErrorException(ServerErrorCode.ICON_STORAGE_ERROR);
        }
    }
}
