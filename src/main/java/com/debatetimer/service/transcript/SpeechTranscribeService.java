package com.debatetimer.service.transcript;

import com.debatetimer.client.storage.SttAudio;
import com.debatetimer.client.storage.SttAudioStorage;
import com.debatetimer.client.stt.SonioxClient;
import com.debatetimer.domain.transcript.SpeechToken;
import com.debatetimer.domain.transcript.UtteranceAssembler;
import com.debatetimer.dto.transcript.response.SpeechTranscribeResponse;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 토론 기록 Soniox 모드에서 타이머가 끝난 발언 구간의 오디오를 받아 문장 단위로 전사한다.
 * 받은 오디오는 토론 세션별로 S3에 비동기로 보관하고, Soniox 전사가 끝날 때까지 요청 스레드에서 기다린다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechTranscribeService {

    // 세션 식별자는 S3 경로에 그대로 들어가므로 경로 문자를 막는다. 프론트는 토론 시작 시 UUID로 만든다.
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("[A-Za-z0-9_-]{1,64}");

    private final SonioxClient sonioxClient;
    private final SttAudioStorage sttAudioStorage;
    private final UtteranceAssembler utteranceAssembler = new UtteranceAssembler();

    public SpeechTranscribeResponse transcribeWithSoniox(long memberId, MultipartFile audio,
                                                        @Nullable String sessionId, @Nullable String language) {
        if (audio == null || audio.isEmpty()) {
            throw new DTClientErrorException(ClientErrorCode.EMPTY_AUDIO);
        }
        if (sessionId != null && !SESSION_ID_PATTERN.matcher(sessionId).matches()) {
            throw new DTClientErrorException(ClientErrorCode.INVALID_STT_SESSION_ID);
        }
        // 전사에 실패한 오디오도 다시 들어볼 수 있도록 전사 전에 보관을 맡긴다.
        store(memberId, sessionId, audio);
        List<SpeechToken> tokens = transcribe(audio, language);
        return new SpeechTranscribeResponse(utteranceAssembler.assemble(tokens));
    }

    // 세션 식별자를 아직 보내지 않는 프론트 요청은 보관 없이 전사만 한다.
    private void store(long memberId, @Nullable String sessionId, MultipartFile audio) {
        if (sessionId == null || !sttAudioStorage.isEnabled()) {
            return;
        }
        try {
            sttAudioStorage.save(new SttAudio(memberId, sessionId, audio.getBytes(), audio.getContentType(),
                    audio.getOriginalFilename()));
        } catch (IOException exception) {
            log.warn("STT 오디오를 읽지 못해 보관하지 않습니다. memberId={}, sessionId={}", memberId, sessionId, exception);
        }
    }

    // Soniox가 오디오를 거절하면 어떤 파일이었는지 알 수 있도록 업로드한 오디오 정보를 함께 남긴다.
    private List<SpeechToken> transcribe(MultipartFile audio, @Nullable String language) {
        try {
            return sonioxClient.transcribe(audio.getResource(), toLanguageHints(language));
        } catch (DTServerErrorException exception) {
            log.warn("Soniox 전사에 실패한 오디오입니다. name={}, contentType={}, size={}",
                    audio.getOriginalFilename(), audio.getContentType(), audio.getSize());
            throw exception;
        }
    }

    // 프론트 음성 인식 언어(ko-KR, en-US 등)를 Soniox 언어 힌트로 바꾼다. 기본은 한국어다.
    private List<String> toLanguageHints(@Nullable String language) {
        if (language != null && language.toLowerCase().startsWith("en")) {
            return List.of("en");
        }
        return List.of("ko");
    }
}
