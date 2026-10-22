package com.debatetimer.event.transcript;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import com.debatetimer.service.transcript.DebateAnalyzer;
import com.debatetimer.service.transcript.TranscriptRefiner;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class TranscriptAnalysisListenerTest {

    @Test
    void 속기록을_먼저_다듬은_뒤_다듬은_속기록으로_분석한다() {
        TranscriptRefiner refiner = mock(TranscriptRefiner.class);
        DebateAnalyzer analyzer = mock(DebateAnalyzer.class);
        TranscriptAnalysisListener listener = new TranscriptAnalysisListener(refiner, analyzer);

        listener.handleTranscriptCreated(new TranscriptCreatedEvent(3L));

        InOrder order = inOrder(refiner, analyzer);
        order.verify(refiner).refine(3L);
        order.verify(analyzer).analyze(3L);
    }
}
