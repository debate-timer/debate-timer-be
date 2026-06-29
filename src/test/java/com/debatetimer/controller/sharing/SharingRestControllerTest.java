package com.debatetimer.controller.sharing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.controller.BaseControllerTest;
import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.customize.response.CustomizeTableResponse;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SharingRestControllerTest extends BaseControllerTest {

    @Nested
    class GetTable {

        @Test
        void 비회원이_사용자_지정_테이블을_조회한다() {
            Member bito = memberGenerator.generate("default@gmail.com");
            CustomizeTableEntity bitoTable = customizeTableEntityGenerator.generate(bito);
            customizeTimeBoxEntityGenerator.generate(bitoTable, CustomizeBoxType.NORMAL, 1);
            customizeTimeBoxEntityGenerator.generateNotExistSpeaker(bitoTable, CustomizeBoxType.NORMAL, 2);

            CustomizeTableResponse response = given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", bitoTable.getId())
                    .when().get("/api/live/table/customize/{tableId}")
                    .then().statusCode(200)
                    .extract().as(CustomizeTableResponse.class);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(bitoTable.getId()),
                    () -> assertThat(response.table()).hasSize(2)
            );
        }

        @Test
        void 존재하지_않는_테이블을_조회하면_예외가_발생한다() {
            long notExistTableId = 999L;

            given()
                    .contentType(ContentType.JSON)
                    .pathParam("tableId", notExistTableId)
                    .when().get("/api/live/table/customize/{tableId}")
                    .then().statusCode(404);
        }
    }
}
