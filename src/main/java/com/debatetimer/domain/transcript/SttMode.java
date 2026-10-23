package com.debatetimer.domain.transcript;

/**
 * 발언을 텍스트로 바꾼 음성 인식 방식. 두 방식의 속기록 품질을 비교하려고 속기록마다 기록한다.
 */
public enum SttMode {

    // 크롬 Web Speech API(구글 음성 인식 RPC)로 발언 중 실시간 인식
    RPC,
    // 발언 구간 오디오를 구간이 끝난 뒤 Soniox 비동기 API로 일괄 전사
    SONIOX,
}
