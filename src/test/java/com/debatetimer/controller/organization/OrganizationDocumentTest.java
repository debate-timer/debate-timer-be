package com.debatetimer.controller.organization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

import com.debatetimer.controller.BaseDocumentTest;
import com.debatetimer.controller.RestDocumentationRequest;
import com.debatetimer.controller.RestDocumentationResponse;
import com.debatetimer.controller.Tag;
import com.debatetimer.dto.organization.OrganizationResponse;
import com.debatetimer.dto.organization.OrganizationResponses;
import com.debatetimer.dto.organization.OrganizationTemplateResponse;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class OrganizationDocumentTest extends BaseDocumentTest {

    @Nested
    class GetOrganizationTemplates {

        private final RestDocumentationRequest requestDocument = request()
                .tag(Tag.ORGANIZATION_API)
                .summary("기관별 템플릿 조회")
                .queryParameter(
                        parameterWithName("language").optional()
                                .description("조회 언어 (US_EN | KO_KR, 기본값 KO_KR)")
                );

        private final RestDocumentationResponse responseDocument = response()
                .responseBodyField(
                        fieldWithPath("organizations").type(ARRAY).description("기관 정보"),
                        fieldWithPath("organizations[].organization").type(STRING).description("기관 명"),
                        fieldWithPath("organizations[].affiliation").type(STRING).description("소속"),
                        fieldWithPath("organizations[].iconPath").type(STRING).description("아이콘 경로 (해당 경로로 서버 요청)"),
                        fieldWithPath("organizations[].templates").type(ARRAY).description("기관 템플릿 목록"),
                        fieldWithPath("organizations[].templates[].name").type(STRING).description("템플릿 명"),
                        fieldWithPath("organizations[].templates[].data").type(STRING).description("템플릿 데이터")
                );

        private static final String DEFAULT_TEMPLATE_CONTENT = "eJyrVspMUbIytjDXUcrMS8tXsqpWykvMTVWyUjJWKCtWMFZ427b1TXPj27YFrxcueN3T8HZWj8LbGVPfdM9V0lEqqSwAqXQODQ7x9%2FWMcgUKJaan5qUkAgWB7IKi%2FOKQ1MRcP4iBbzasedOyESienJ%2BHLP56wwygwUDx8sSivMy8dKfUnBwlq7TEnOJUHaW0zLzM4gwkoVqgtYlJOUCN0dVKxSWJeckgMwKC%2FIOBJhQXpKYmZ4RAnPVmXivQzUDRpPwKqJCff5Cvow%2FI5Zkgqw2NDICyYLPzSnNyIMIBqUUgx6EJBRekJmYDHQcTLgbxU4sg3FodJKc4%2B%2FsNFqf4uYaGBIEtQXPNhDdzFkCiFMVNIZ6%2BrvFOjsGuLnB3QazA6TBzkLMx3AX2DIkhtHXOm0WtCq83zHkzbQfdAwpr8qG7i2JrAbdLRw0%3D";

        @Test
        void 기관_템플릿_조회_성공() {
            OrganizationResponses response = new OrganizationResponses(List.of(
                    new OrganizationResponse("한앎", "한양대", "/icons/icon1.png", List.of(
                            new OrganizationTemplateResponse("템플릿1", DEFAULT_TEMPLATE_CONTENT),
                            new OrganizationTemplateResponse("템플릿2", DEFAULT_TEMPLATE_CONTENT)
                    )),
                    new OrganizationResponse("한모름", "양한대", "/icons/icon2.png", List.of(
                            new OrganizationTemplateResponse("템플릿1", DEFAULT_TEMPLATE_CONTENT),
                            new OrganizationTemplateResponse("템플릿2", DEFAULT_TEMPLATE_CONTENT)
                    ))
            ));
            doReturn(response).when(organizationService).findAll(any());

            var document = document("organization/get-templates", 200)
                    .request(requestDocument)
                    .response(responseDocument)
                    .build();

            given(document)
                    .contentType(ContentType.JSON)
                    .queryParam("language", "KO_KR")
                    .when().get("/api/organizations/templates")
                    .then().statusCode(200);
        }
    }
}
