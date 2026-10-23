package com.debatetimer.domain.transcript;

import com.debatetimer.domain.customize.Stance;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 타이머가 한 번 실행된 동안 인식한 발언 묶음. team은 자유토론처럼 한 타임박스 안에서 발언 팀이 바뀌는 경우에만 채워진다.
 */
public record SpeechSegment(int timeBoxIndex, @Nullable Stance team, List<Utterance> utterances) {
}
