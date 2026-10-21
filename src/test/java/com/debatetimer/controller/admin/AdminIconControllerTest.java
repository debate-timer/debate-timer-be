package com.debatetimer.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.client.storage.IconStorageProperties;
import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.dto.admin.IconResponse;
import com.debatetimer.dto.admin.IconResponses;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
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
                .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
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
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .multiPart("file", "original.png", PNG, "image/png")
                    .multiPart("name", "duplicated")
                    .when().post("/api/admin/icons")
                    .then().statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 기본_아이콘과_같은_이름이면_업로드할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .multiPart("file", "original.png", PNG, "image/png")
                    .multiPart("name", "ceda")
                    .when().post("/api/admin/icons")
                    .then().statusCode(HttpStatus.CONFLICT.value());
        }

        @Test
        void 이미지가_아니면_업로드할_수_없다() {
            given()
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
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
                    .header(AdminSharingController.ADMIN_PASSWORD_HEADER, PASSWORD)
                    .when().get("/api/admin/icons")
                    .then().statusCode(HttpStatus.OK.value())
                    .extract().as(IconResponses.class);

            assertThat(response.icons()).contains(
                    new IconResponse("/icon/ceda.png", false),
                    new IconResponse("/icon/uploaded_icon.png", true)
            );
        }
    }
}
