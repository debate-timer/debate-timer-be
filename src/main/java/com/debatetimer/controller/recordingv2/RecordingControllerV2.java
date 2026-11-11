package com.debatetimer.controller.recordingv2;

import com.debatetimer.controller.auth.AuthMember;
import com.debatetimer.domain.member.FeatureFlag;
import com.debatetimer.domain.member.Member;
import com.debatetimer.dto.recordingv2.request.RecordingJoinRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingSessionCreateRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingTurnUploadRequestV2;
import com.debatetimer.dto.recordingv2.response.RecordingDeviceResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionCreateResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionResponseV2;
import com.debatetimer.service.recordingv2.RecordingServiceV2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 다기기 녹음 V2. 세션 생성·조회·종료는 사회자(회원)만, 참가·업로드는 참가 코드·기기 토큰으로 청중도 호출한다.
 */
@RestController
@RequiredArgsConstructor
public class RecordingControllerV2 {

    public static final String DEVICE_TOKEN_HEADER = "X-Recording-Device";

    private final RecordingServiceV2 recordingServiceV2;

    @PostMapping("/api/v2/recordings")
    @ResponseStatus(HttpStatus.CREATED)
    public RecordingSessionCreateResponseV2 createSession(
            @AuthMember(requires = FeatureFlag.MULTI_DEVICE_RECORDING) Member member,
            @RequestBody @Valid RecordingSessionCreateRequestV2 request
    ) {
        return recordingServiceV2.create(member, request);
    }

    @GetMapping("/api/v2/recordings/{sessionKey}")
    @ResponseStatus(HttpStatus.OK)
    public RecordingSessionResponseV2 getSession(
            @AuthMember(requires = FeatureFlag.MULTI_DEVICE_RECORDING) Member member,
            @PathVariable(name = "sessionKey") String sessionKey
    ) {
        return recordingServiceV2.get(member, sessionKey);
    }

    @PostMapping("/api/v2/recordings/{sessionKey}/host-device")
    @ResponseStatus(HttpStatus.CREATED)
    public RecordingDeviceResponseV2 rejoinHost(
            @AuthMember(requires = FeatureFlag.MULTI_DEVICE_RECORDING) Member member,
            @PathVariable(name = "sessionKey") String sessionKey
    ) {
        return recordingServiceV2.rejoinHost(member, sessionKey);
    }

    @PostMapping("/api/v2/recordings/{sessionKey}/finish")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void finish(
            @AuthMember(requires = FeatureFlag.MULTI_DEVICE_RECORDING) Member member,
            @PathVariable(name = "sessionKey") String sessionKey
    ) {
        recordingServiceV2.finish(member, sessionKey);
    }

    // 청중 기기는 로그인 없이 참가 코드로 참가한다.
    @PostMapping("/api/v2/recordings/{sessionKey}/devices")
    @ResponseStatus(HttpStatus.CREATED)
    public RecordingDeviceResponseV2 join(
            @PathVariable(name = "sessionKey") String sessionKey,
            @RequestBody @Valid RecordingJoinRequestV2 request
    ) {
        return recordingServiceV2.join(sessionKey, request);
    }

    // 업로드는 받아 두기만 하고 바로 응답한다. 고르기·전사는 백그라운드에서 한다.
    @PostMapping(value = "/api/v2/recordings/{sessionKey}/turns/{turnKey}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void uploadTurn(
            @PathVariable(name = "sessionKey") String sessionKey,
            @PathVariable(name = "turnKey") String turnKey,
            @RequestHeader(name = DEVICE_TOKEN_HEADER, required = false) String deviceToken,
            @Valid @ModelAttribute RecordingTurnUploadRequestV2 request,
            @RequestPart(name = "audio", required = false) MultipartFile audio
    ) {
        recordingServiceV2.upload(sessionKey, deviceToken, turnKey, request, audio);
    }
}
