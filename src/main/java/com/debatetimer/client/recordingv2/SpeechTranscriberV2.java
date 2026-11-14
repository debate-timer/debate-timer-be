package com.debatetimer.client.recordingv2;

import com.debatetimer.client.stt.SonioxClient;
import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.domain.transcript.UtteranceAssembler;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.Nullable;

/**
 * 고른 후보 오디오 파일을 Soniox로 전사해 문장 단위로 묶는다.
 * V1 동기 전사의 동시 전사 한도를 나눠 쓰지 않도록 V2 전용 SonioxClient 인스턴스를 받는다.
 */
public class SpeechTranscriberV2 {

    private final SonioxClient sonioxClient;
    private final UtteranceAssembler utteranceAssembler = new UtteranceAssembler();

    public SpeechTranscriberV2(SonioxClient sonioxClient) {
        this.sonioxClient = sonioxClient;
    }

    public List<Utterance> transcribe(Path file, @Nullable String language) {
        List<SpeechToken> tokens = sonioxClient.transcribe(new FileSystemResource(file), toLanguageHints(language));
        return utteranceAssembler.assemble(tokens);
    }

    // 프론트 음성 인식 언어(ko-KR, en-US 등)를 Soniox 언어 힌트로 바꾼다. 기본은 한국어다.
    private List<String> toLanguageHints(@Nullable String language) {
        if (language != null && language.toLowerCase().startsWith("en")) {
            return List.of("en");
        }
        return List.of("ko");
    }
}
