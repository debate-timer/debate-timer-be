package com.debatetimer.exception.errorcode;

import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.parliamentary.ParliamentaryTable;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ClientErrorCode implements ErrorCode {

    INVALID_MEMBER_NICKNAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "닉네임은 1자 이상 %d자 이하여야 합니다".formatted(Member.NICKNAME_MAX_LENGTH)
    ),
    INVALID_MEMBER_NICKNAME_FORM(HttpStatus.BAD_REQUEST, "닉네임은 영문/한글만 가능합니다"),

    INVALID_TABLE_NAME_LENGTH(
            HttpStatus.BAD_REQUEST,
            "테이블 이름은 1자 이상 %d자 이하여야 합니다".formatted(ParliamentaryTable.NAME_MAX_LENGTH)
    ),
    INVALID_TABLE_NAME_FORM(
            HttpStatus.BAD_REQUEST,
            "테이블 이름은 영문/한글만 가능합니다"
    ),
    INVALID_TABLE_TIME(HttpStatus.BAD_REQUEST, "시간은 양수만 가능합니다"),

    INVALID_TIME_BOX_SEQUENCE(HttpStatus.BAD_REQUEST, "순서는 양수만 가능합니다"),
    INVALID_TIME_BOX_TIME(HttpStatus.BAD_REQUEST, "시간은 양수만 가능합니다"),
    INVALID_TIME_BOX_STANCE(HttpStatus.BAD_REQUEST, "타임박스 유형과 일치하지 않는 입장입니다."),
    INVALID_TIME_BOX_TYPE(HttpStatus.BAD_REQUEST, "잘못된 타임 박스 유형입니다."),

    FIELD_ERROR(HttpStatus.BAD_REQUEST, "입력이 잘못되었습니다."),
    URL_PARAMETER_ERROR(HttpStatus.BAD_REQUEST, "입력이 잘못되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "입력한 값의 타입이 잘못되었습니다."),
    NO_RESOURCE_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_SUPPORTED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),
    MEDIA_TYPE_NOT_SUPPORTED(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "허용되지 않은 미디어 타입입니다."),
    ALREADY_DISCONNECTED(HttpStatus.BAD_REQUEST, "이미 클라이언트에서 요청이 종료되었습니다."),

    TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "토론 테이블을 찾을 수 없습니다."),
    NOT_TABLE_OWNER(HttpStatus.UNAUTHORIZED, "테이블을 소유한 회원이 아닙니다."),
    UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "접근 권한이 없습니다"),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원이 존재하지 않습니다"),
    ;

    private final HttpStatus status;
    private final String message;

    ClientErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
