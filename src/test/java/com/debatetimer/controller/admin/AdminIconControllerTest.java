package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.client.storage.IconStorageProperties;
import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.organization.OrganizationIcon;
import com.debatetimer.dto.admin.IconResponse;
import com.debatetimer.dto.admin.IconResponses;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class AdminIconControllerTest extends BaseControllerTest {

    private static final String PASSWORD = "test-admin-password";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0};

    @Autowired
    private IconStorageProperties iconStorageProperties;

    @AfterEach
    void cleanUploadedIcons() throws IOException {
        try (Stream<Path> files = Files.list(iconStorageProperties.getUploadDir())) {
            for (Path file : files.toList()) {
                Files.deleteIfExists(file);
            }
        }
    }

    private IconResponse upload(String name) {
        return given()
                .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                .multiPart("file", "original.png", PNG, "image/png")
                .multiPart("name", name)
                .when().post("/api/admin/icons")
                .then().statusCode(HttpStatus.CREATED.value())
                .extract().as(IconResponse.class);
    }

    @Nested
    class UploadIcon {

        @Test
        void 아이콘을_업로드하면_아이콘_경로로_제공한다() {
            IconResponse response = upload("new_icon");

            byte[] served = given()
                    .when().get(response.path())
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().asByteArray();

            assertAll(
                    () -> assertThat(response.path()).isEqualTo("/icon/new_icon.png"),
                    () -> assertThat(served).isEqualTo(PNG)
            );
        }

        @Test
        void 같은_이름의_아이콘이_있으면_업로드할_수_없다() {
            upload("duplicated");

            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .multiPart("file", "original.png", PNG, "image/png")
                    .multiPart("name", "duplicated")
                    .when().post("/api/admin/icons")
                    .then().statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 기본_아이콘과_같은_이름이면_업로드할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .multiPart("file", "original.png", PNG, "image/png")
                    .multiPart("name", "ceda")
                    .when().post("/api/admin/icons")
                    .then().statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 전체_업로드_한도와_별개로_아이콘이_2MB를_넘으면_업로드할_수_없다() {
            byte[] largePng = Arrays.copyOf(PNG, OrganizationIcon.MAX_SIZE_BYTES + 1);

            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .multiPart("file", "large.png", largePng, "image/png")
                    .multiPart("name", "large")
                    .when().post("/api/admin/icons")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 이미지가_아니면_업로드할_수_없다() {
            given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .multiPart("file", "evil.svg", "<svg></svg>".getBytes(), "image/svg+xml")
                    .multiPart("name", "evil")
                    .when().post("/api/admin/icons")
                    .then().statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 어드민_비밀번호가_없으면_업로드할_수_없다() {
            given()
                    .multiPart("file", "original.png", PNG, "image/png")
                    .when().post("/api/admin/icons")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class GetIcons {

        @Test
        void 기본_아이콘과_업로드한_아이콘을_함께_조회한다() {
            upload("uploaded_icon");

            IconResponses response = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/icons")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(IconResponses.class);

            assertThat(response.icons()).contains(
                    new IconResponse("/icon/ceda.png", false),
                    new IconResponse("/icon/uploaded_icon.png", true)
            );
        }
    }

    @Nested
    class DownloadIcons {

        @Test
        void 기본_아이콘과_업로드한_아이콘을_원본_그대로_ZIP으로_내려받는다() throws IOException {
            upload("zipped_icon");

            byte[] zip = given()
                    .header(AdminAuthInterceptor.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/icons/download")
                    .then().statusCode(HttpStatus.OK.value())
                    .contentType("application/zip")
                    .header("Content-Disposition", "attachment; filename=\"organization-icons.zip\"")
                    .extract().asByteArray();

            Map<String, byte[]> entries = unzip(zip);
            assertAll(
                    () -> assertThat(entries).containsKey("ceda.png"),
                    () -> assertThat(entries.get("zipped_icon.png")).isEqualTo(PNG)
            );
        }

        @Test
        void 어드민_비밀번호가_없으면_내려받을_수_없다() {
            given()
                    .when().get("/api/admin/icons/download")
                    .then().statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        private Map<String, byte[]> unzip(byte[] zip) throws IOException {
            Map<String, byte[]> entries = new HashMap<>();
            try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(zip))) {
                ZipEntry entry;
                while ((entry = input.getNextEntry()) != null) {
                    entries.put(entry.getName(), input.readAllBytes());
                }
            }
            return entries;
        }
    }
}
