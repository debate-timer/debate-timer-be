package com.debatetimer.service.transcript;

import com.debatetimer.client.stt.SonioxClient;
import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.domain.transcript.UtteranceAssembler;
import com.debatetimer.dto.transcript.response.SpeechTranscribeResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 토론 기록 Soniox 모드에서 타이머가 끝난 발언 구간의 오디오를 받아 문장 단위로 전사한다.
 * Soniox 전사가 끝날 때까지 요청 스레드에서 기다린다.
 */
@Service
@RequiredArgsConstructor
public class SpeechTranscribeService {

    private final SonioxClient sonioxClient;
    private final UtteranceAssembler utteranceAssembler = new UtteranceAssembler();

    public SpeechTranscribeResponse transcribeWithSoniox(MultipartFile audio, @Nullable String language) {
        if (audio == null || audio.isEmpty()) {
            throw new DTClientErrorException(ClientErrorCode.EMPTY_AUDIO);
        }
        List<SpeechToken> tokens = sonioxClient.transcribe(audio.getResource(), toLanguageHints(language));
        return new SpeechTranscribeResponse(utteranceAssembler.assemble(tokens));
    }

    // 프론트 음성 인식 언어(ko-KR, en-US 등)를 Soniox 언어 힌트로 바꾼다. 기본은 한국어다.
    private List<String> toLanguageHints(@Nullable String language) {
        if (language != null && language.toLowerCase().startsWith("en")) {
            return List.of("en");
        }
        return List.of("ko");
    }
}
