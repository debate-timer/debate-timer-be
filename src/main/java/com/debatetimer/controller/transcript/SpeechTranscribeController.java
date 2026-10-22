package com.debatetimer.controller.transcript;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.transcript.response.SpeechTranscribeResponse;
import com.debatetimer.service.transcript.SpeechTranscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class SpeechTranscribeController {

    private final SpeechTranscribeService speechTranscribeService;

    // 회원만 호출할 수 있게 인증 회원을 받는다. 외부 STT 비용이 드는 요청이다.
    @PostMapping(value = "/api/stt/soniox", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public SpeechTranscribeResponse transcribeWithSoniox(
            @AuthMember Member member,
            @RequestPart("audio") MultipartFile audio,
            @RequestParam(name = "language", required = false) String language
    ) {
        return speechTranscribeService.transcribeWithSoniox(audio, language);
    }
}
