package com.debatetimer.exception.errorcode;

import com.debatetimer.domain.customize.Agenda;
import com.debatetimer.domain.customize.TableName;
import com.debatetimer.domain.customize.TeamName;
import com.debatetimer.entity.customize.CustomizeTimeBox;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ClientErrorCode implements ResponseErrorCode {

    INVALID_TABLE_NAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "테이블 이름은 1자 이상 %d자 이하여야 합니다".formatted(TableName.NAME_MAX_LENGTH)
    ),
    INVALID_TABLE_NAME_FORM(
            HttpStatus.BAD_REQUEST,
            "테이블 이름에 이모지를 넣을 수 없습니다"
    ),
    INVALID_TABLE_TIME(HttpStatus.BAD_REQUEST, "시간은 양수만 가능합니다"),

    INVALID_TIME_BOX_SEQUENCE(HttpStatus.BAD_REQUEST, "순서는 양수만 가능합니다"),
    INVALID_TIME_BOX_SPEAKER(HttpStatus.BAD_REQUEST, "발표자 번호는 양수만 가능합니다"),
    INVALID_TIME_BOX_TIME(HttpStatus.BAD_REQUEST, "시간은 양수만 가능합니다"),
    INVALID_TIME_BOX_STANCE(HttpStatus.BAD_REQUEST, "타임박스 유형과 일치하지 않는 입장입니다."),
    INVALID_TIME_BOX_FORMAT(HttpStatus.BAD_REQUEST, "타임박스 유형과 일치하지 않는 형식입니다"),
    INVALID_TIME_BASED_TIME(HttpStatus.BAD_REQUEST, "팀 발언 시간은 개인 발언 시간보다 길어야합니다"),
    INVALID_TIME_BOX_SPEECH_TYPE_LENGTH(
            HttpStatus.BAD_REQUEST,
            "발언 유형 이름은 1자 이상 %d자 이하여야 합니다.".formatted(CustomizeTimeBox.SPEECH_TYPE_MAX_LENGTH)
    ),
    INVALID_TIME_BOX_SPEAKER_LENGTH(
            HttpStatus.BAD_REQUEST,
            "발언자 이름은 1자 이상 %d자 이하여야 합니다.".formatted(CustomizeTimeBox.SPEAKER_MAX_LENGTH)
    ),
    INVALID_TEAM_NAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "팀 이름은 1자 이상 %d자 이하여야 합니다.".formatted(TeamName.NAME_MAX_LENGTH)
    ),
    INVALID_TEAM_NAME_FORM(
            HttpStatus.BAD_REQUEST,
            "팀 이름에 이모지를 넣을 수 없습니다"
    ),
    INVALID_AGENDA_LENGTH(
            HttpStatus.BAD_REQUEST,
            "토론 주제는 1자 이상 %d자 이하여야 합니다.".formatted(Agenda.AGENDA_MAX_LENGTH)
    ),

    TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "토론 테이블을 찾을 수 없습니다."),
    NOT_TABLE_OWNER(HttpStatus.UNAUTHORIZED, "테이블을 소유한 회원이 아닙니다."),
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 기한이 만료되었습니다"),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원이 존재하지 않습니다"),

    INVALID_OAUTH_REQUEST(HttpStatus.BAD_REQUEST, "구글 OAuth 요청 파라미터 값이 잘못되었습니다."),

    FIELD_ERROR(HttpStatus.BAD_REQUEST, "입력이 잘못되었습니다."),
    URL_PARAMETER_ERROR(HttpStatus.BAD_REQUEST, "입력이 잘못되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "입력한 값의 타입이 잘못되었습니다."),
    NO_RESOURCE_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "허용되지 않은 미디어 타입입니다."),
    ALREADY_DISCONNECTED(HttpStatus.BAD_REQUEST, "이미 클라이언트에서 요청이 종료되었습니다."),
    NO_COOKIE_FOUND(HttpStatus.BAD_REQUEST, "필수 쿠키 값이 존재하지 않습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "파일 업로드에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    ClientErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
