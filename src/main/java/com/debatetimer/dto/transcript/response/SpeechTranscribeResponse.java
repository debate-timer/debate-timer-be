package com.debatetimer.dto.transcript.response;

import com.debatetimer.domain.transcript.Utterance;
import java.util.List;

/**
 * 발언 구간 오디오를 전사한 문장 목록. offsetMs는 구간 오디오 시작 시점 기준이다.
 */
public record SpeechTranscribeResponse(List<Utterance> utterances) {
}
