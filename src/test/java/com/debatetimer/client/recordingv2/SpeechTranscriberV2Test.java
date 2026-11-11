package com.debatetimer.client.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.debatetimer.client.stt.SonioxClient;
import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.domain.transcript.Utterance;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

class SpeechTranscriberV2Test {

    private final SonioxClient sonioxClient = mock(SonioxClient.class);
    private final SpeechTranscriberV2 transcriber = new SpeechTranscriberV2(sonioxClient);

    @Test
    void 파일을_전사해_문장_단위로_묶는다() {
        when(sonioxClient.transcribe(any(Resource.class), eq(List.of("ko"))))
                .thenReturn(List.of(new SpeechToken("안녕", 0, 100), new SpeechToken("하세요.", 100, 300)));

        List<Utterance> utterances = transcriber.transcribe(Path.of("turn.webm"), "ko-KR");

        assertThat(utterances).containsExactly(new Utterance(0, "안녕하세요."));
    }

    @Test
    void 영어면_영어_힌트로_전사한다() {
        when(sonioxClient.transcribe(any(Resource.class), any())).thenReturn(List.of());

        transcriber.transcribe(Path.of("turn.webm"), "en-US");

        verify(sonioxClient).transcribe(any(Resource.class), eq(List.of("en")));
    }
}
