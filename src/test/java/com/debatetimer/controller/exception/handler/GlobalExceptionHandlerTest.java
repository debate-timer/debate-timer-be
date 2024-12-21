package com.debatetimer.controller.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.debatetimer.BaseControllerTest;
import com.debatetimer.controller.exception.ErrorResponse;
import com.debatetimer.controller.exception.custom.DTBadRequestException;
import com.debatetimer.controller.exception.custom.DTException;
import com.debatetimer.controller.exception.custom.DTNotFoundException;
import com.debatetimer.controller.exception.custom.DTServerErrorException;
import com.debatetimer.controller.exception.custom.DTUnauthorizedException;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest extends BaseControllerTest {

    @MockitoBean
    private TestController testController;

    @Nested
    class handleMethodArgumentNotValidException {

        @Test
        void 상태코드_400_을_반환한다() throws Exception {
            MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
            doReturn(List.of())
                    .when(exception)
                    .getAllErrors();

            doThrow(exception)
                    .when(testController)
                    .testMethod();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/test")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    class handleNoResourceFoundException {

        @Test
        void 상태코드_404를_반환한다() throws Exception {
            doThrow(NoResourceFoundException.class)
                    .when(testController)
                    .testMethod();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/test")
                    .then()
                    .statusCode(HttpStatus.NOT_FOUND.value());
        }
    }

    @Nested
    class handleException {

        @Test
        void 상태코드_500을_반환한다() throws Exception {
            doThrow(Exception.class)
                    .when(testController)
                    .testMethod();

            RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/test")
                    .then()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Nested
    class handleCustomException {

        @ParameterizedTest
        @MethodSource("customExceptionProvider")
        void 커스텀_예외의_상태코드와_메시지를_담은_응답객체를_반환한다(DTException dtException) throws Exception {
            int expectedCode = dtException.getHttpStatus().value();
            String expectedMessage = dtException.getMessage();
            doThrow(dtException)
                    .when(testController)
                    .testMethod();

            ErrorResponse actualResponse = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/test")
                    .then()
                    .statusCode(expectedCode)
                    .extract()
                    .as(ErrorResponse.class);

            assertThat(actualResponse.message()).isEqualTo(expectedMessage);
        }

        static Stream<DTException> customExceptionProvider() {
            return Stream.of(
                    new DTBadRequestException("badRequestException"),
                    new DTUnauthorizedException("unAuthorizedException"),
                    new DTServerErrorException("serverErrorException"),
                    new DTNotFoundException("notFoundException")
            );
        }
    }
}
