package com.debatetimer.domain.sharing;

/**
 * 서버가 사회자 채널(`/chairman/{roomId}`)로 보내는 알림 유형
 */
public enum ChairmanNoticeType {

    /**
     * 새 청중이 입장해 현재 상태 공유가 필요하다.
     */
    SYNC_REQUEST,

    /**
     * 다른 사회자 세션이 활성 사회자가 되어 발행 권한을 잃었다.
     */
    REPLACED,
    ;
}
