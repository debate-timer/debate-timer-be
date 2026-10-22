package com.debatetimer.event.transcript;

import com.debatetimer.service.transcript.DebateAnalyzer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 속기록이 저장된 뒤 별도 스레드에서 분석 보고서를 만든다. 분석은 수십 초가 걸릴 수 있어 요청과 분리한다.
 */
@Component
@RequiredArgsConstructor
public class TranscriptAnalysisListener {

    private final DebateAnalyzer debateAnalyzer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleTranscriptCreated(TranscriptCreatedEvent event) {
        debateAnalyzer.analyze(event.transcriptId());
    }
}
