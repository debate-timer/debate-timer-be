package com.debatetimer.scheduler.recordingv2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.debatetimer.client.recordingv2.AudioSpoolV2;
import com.debatetimer.config.recordingv2.RecordingV2Properties;
import com.debatetimer.domain.recordingv2.ClosedTurnV2;
import com.debatetimer.service.recordingv2.RecordingFinalizerV2;
import com.debatetimer.service.recordingv2.RecordingSessionRegistryV2;
import com.debatetimer.service.recordingv2.TurnProcessorV2;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecordingV2SchedulerTest {

    private static final RecordingV2Properties PROPERTIES = new RecordingV2Properties(true, null, null, null, null,
            null, null, null, null, null, null);

    @Test
    void 한_턴_처리에서_오류가_나도_같이_닫힌_다른_턴을_처리한다() {
        RecordingSessionRegistryV2 registry = mock(RecordingSessionRegistryV2.class);
        TurnProcessorV2 processor = mock(TurnProcessorV2.class);
        ClosedTurnV2 broken = new ClosedTurnV2(1L, "session", "0-1", List.of());
        ClosedTurnV2 next = new ClosedTurnV2(1L, "session", "1-2", List.of());
        when(registry.drainClosable(any(), any())).thenReturn(List.of(broken, next));
        doThrow(new RuntimeException("bug")).when(processor).submit(broken);
        RecordingV2Scheduler scheduler = new RecordingV2Scheduler(PROPERTIES, registry, processor,
                mock(RecordingFinalizerV2.class), mock(AudioSpoolV2.class));

        scheduler.closeTurns();

        verify(processor).submit(next);
    }
}
