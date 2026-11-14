package com.debatetimer.exception.errorcode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ServerErrorCode implements ResponseErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다. 관리자에게 문의하세요."),
    EXCEL_EXPORT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "엑셀 변환 과정에서 오류가 발생하였습니다"),
    OAUTH_REQUEST_FAILED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "구글 OAuth 요청 과정에 오류가 발생하였습니다"),
    ICON_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "아이콘 파일 저장소 처리 중 오류가 발생하였습니다"),
    STT_NOT_CONFIGURED(HttpStatus.SERVICE_UNAVAILABLE, "음성 인식 API 키가 설정되지 않았습니다"),
    STT_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "음성 인식 요청에 실패했습니다"),
    RECORDING_TURN_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "녹음 턴을 찾을 수 없습니다"),
    RECORDING_UTTERANCES_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "녹음 전사 결과 변환 과정에서 오류가 발생하였습니다"),
    RECORDING_SECRET_HASH_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "녹음 참가 코드·기기 토큰 해시 과정에서 오류가 발생하였습니다"),
    RECORDING_AUDIO_SPOOL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "녹음 오디오 임시 저장 과정에서 오류가 발생하였습니다");

    private final HttpStatus status;
    private final String message;

    ServerErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
