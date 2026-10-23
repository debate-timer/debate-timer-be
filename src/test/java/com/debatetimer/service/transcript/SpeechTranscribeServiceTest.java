package com.debatetimer.service.transcript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.debatetimer.client.stt.SonioxClient;
import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.dto.transcript.response.SpeechTranscribeResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.service.BaseServiceTest;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class SpeechTranscribeServiceTest extends BaseServiceTest {

    private static final MockMultipartFile AUDIO = new MockMultipartFile("audio", "segment.webm", "audio/webm",
            new byte[]{1, 2, 3});

    @Autowired
    private SpeechTranscribeService speechTranscribeService;

    @MockitoBean
    private SonioxClient sonioxClient;

    @Nested
    class TranscribeWithSoniox {

        @Test
        void Soniox_토큰을_문장_단위_발화로_묶어_돌려준다() {
            when(sonioxClient.transcribe(any(), eq(List.of("ko")))).thenReturn(List.of(
                    new SpeechToken("찬성", 100, 400),
                    new SpeechToken(" 입론입니다.", 400, 900),
                    new SpeechToken(" 감사합니다.", 1_200, 1_600)
            ));

            SpeechTranscribeResponse response = speechTranscribeService.transcribeWithSoniox(AUDIO, "ko-KR");

            assertThat(response.utterances()).containsExactly(
                    new Utterance(100, "찬성 입론입니다."),
                    new Utterance(1_200, "감사합니다.")
            );
        }

        @Test
        void 영어_화면이면_영어_언어_힌트로_요청한다() {
            when(sonioxClient.transcribe(any(), any())).thenReturn(List.of());

            speechTranscribeService.transcribeWithSoniox(AUDIO, "en-US");

            verify(sonioxClient).transcribe(any(), eq(List.of("en")));
        }

        @Test
        void 오디오가_비어_있으면_전사하지_않고_예외를_던진다() {
            MockMultipartFile empty = new MockMultipartFile("audio", "segment.webm", "audio/webm", new byte[0]);

            assertThatThrownBy(() -> speechTranscribeService.transcribeWithSoniox(empty, "ko-KR"))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.EMPTY_AUDIO.getMessage());
            verifyNoInteractions(sonioxClient);
        }
    }
}
