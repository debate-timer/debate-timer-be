package com.debatetimer.service.recordingv2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.debatetimer.domain.customize.CustomizeBoxType;
import com.debatetimer.domain.member.Member;
import com.debatetimer.domain.recordingv2.ClosedTurnV2;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domainrepository.recordingv2.RecordingV2DomainRepository;
import com.debatetimer.dto.recordingv2.request.RecordingJoinRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingSessionCreateRequestV2;
import com.debatetimer.dto.recordingv2.request.RecordingTurnUploadRequestV2;
import com.debatetimer.dto.recordingv2.response.RecordingDeviceResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionCreateResponseV2;
import com.debatetimer.dto.recordingv2.response.RecordingSessionResponseV2;
import com.debatetimer.entity.customize.CustomizeTableEntity;
import com.debatetimer.exception.custom.DTClientErrorException;
import com.debatetimer.exception.errorcode.ClientErrorCode;
import com.debatetimer.scheduler.recordingv2.RecordingV2Scheduler;
import com.debatetimer.service.BaseServiceTest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = {
        "recording-v2.enabled=true",
        "recording-v2.spool-dir=build/test-recording-v2",
        "recording-v2.max-devices=3"
})
class RecordingServiceV2Test extends BaseServiceTest {

    private static final RecordingTurnUploadRequestV2 UPLOAD = new RecordingTurnUploadRequestV2(0, null, 10_000L,
            1_000L, -20.0, -3.0, 0.0, 0.2, 30.0);

    @Autowired
    private RecordingServiceV2 recordingServiceV2;

    @Autowired
    private RecordingSessionRegistryV2 registry;

    @Autowired
    private RecordingV2DomainRepository recordingV2DomainRepository;

    // 주기 작업이 테스트 중에 턴을 닫아 전사로 넘기지 않게 막는다
    @MockitoBean
    private RecordingV2Scheduler recordingV2Scheduler;

    private Member member;
    private CustomizeTableEntity table;

    @BeforeEach
    void setUp() {
        member = memberGenerator.generate("host@email.com");
        table = customizeTableEntityGenerator.generate(member);
        customizeTimeBoxEntityGenerator.generate(table, CustomizeBoxType.NORMAL, 1);
    }

    @Nested
    class Create {

        @Test
        void 녹음_세션을_열고_사회자_기기를_참가시킨다() {
            RecordingSessionCreateResponseV2 created = create();

            assertAll(
                    () -> assertThat(created.joinCode()).isNotBlank(),
                    () -> assertThat(created.deviceToken()).isNotBlank(),
                    () -> assertThat(recordingV2DomainRepository.getSession(created.sessionKey()).status())
                            .isEqualTo(RecordingSessionStatusV2.RECORDING)
            );
        }

        @Test
        void 다른_회원의_테이블로는_열_수_없다() {
            Member other = memberGenerator.generate("other@email.com");

            assertThatThrownBy(() -> recordingServiceV2.create(other,
                    new RecordingSessionCreateRequestV2(table.getId(), null)))
                    .isInstanceOf(DTClientErrorException.class);
        }
    }

    @Nested
    class Join {

        @Test
        void 참가_코드가_맞으면_기기_토큰을_발급한다() {
            RecordingSessionCreateResponseV2 created = create();

            RecordingDeviceResponseV2 device = recordingServiceV2.join(created.sessionKey(),
                    new RecordingJoinRequestV2(created.joinCode()));

            assertThat(device.deviceToken()).isNotEqualTo(created.deviceToken());
        }

        @Test
        void 참가_코드가_틀리면_참가할_수_없다() {
            RecordingSessionCreateResponseV2 created = create();

            assertThatThrownBy(() -> recordingServiceV2.join(created.sessionKey(),
                    new RecordingJoinRequestV2("wrong")))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_RECORDING_JOIN_CODE.getMessage());
        }

        @Test
        void 끝낸_세션에는_참가할_수_없다() {
            RecordingSessionCreateResponseV2 created = create();
            recordingServiceV2.finish(member, created.sessionKey());

            assertThatThrownBy(() -> recordingServiceV2.join(created.sessionKey(),
                    new RecordingJoinRequestV2(created.joinCode())))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.RECORDING_SESSION_CLOSED.getMessage());
        }
    }

    @Nested
    class Upload {

        @Test
        void 참가한_기기들의_후보를_한_턴으로_모은다() {
            RecordingSessionCreateResponseV2 created = create();
            RecordingDeviceResponseV2 audience = recordingServiceV2.join(created.sessionKey(),
                    new RecordingJoinRequestV2(created.joinCode()));

            recordingServiceV2.upload(created.sessionKey(), created.deviceToken(), "0-1", UPLOAD, audio());
            recordingServiceV2.upload(created.sessionKey(), audience.deviceToken(), "0-1", UPLOAD, audio());

            List<ClosedTurnV2> closed = registry.drainClosable(Duration.ofSeconds(20));
            assertThat(closed).singleElement()
                    .satisfies(turn -> assertThat(turn.candidates()).hasSize(2));
            closed.forEach(turn -> registry.markPersisted(turn.sessionKey()));
        }

        @Test
        void 참가하지_않은_토큰으로는_올릴_수_없다() {
            RecordingSessionCreateResponseV2 created = create();

            assertThatThrownBy(() -> recordingServiceV2.upload(created.sessionKey(), "unknown", "0-1", UPLOAD,
                    audio()))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_RECORDING_DEVICE.getMessage());
        }

        @Test
        void 경로_문자가_섞인_턴_키는_받지_않는다() {
            RecordingSessionCreateResponseV2 created = create();

            assertThatThrownBy(() -> recordingServiceV2.upload(created.sessionKey(), created.deviceToken(),
                    "../0", UPLOAD, audio()))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.INVALID_RECORDING_TURN.getMessage());
        }

        @Test
        void 이미_DB에_기록된_턴에는_올릴_수_없다() {
            RecordingSessionCreateResponseV2 created = create();
            RecordingSessionV2 session = recordingV2DomainRepository.getSession(created.sessionKey());
            recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 1);

            assertThatThrownBy(() -> recordingServiceV2.upload(created.sessionKey(), created.deviceToken(), "0-1",
                    UPLOAD, audio()))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.RECORDING_TURN_CLOSED.getMessage());
        }

        @Test
        void 끝낸_직후에는_늦게_도착한_후보를_받는다() {
            RecordingSessionCreateResponseV2 created = create();
            recordingServiceV2.finish(member, created.sessionKey());

            assertThatCode(() -> recordingServiceV2.upload(created.sessionKey(), created.deviceToken(), "0-1",
                    UPLOAD, audio()))
                    .doesNotThrowAnyException();
            registry.remove(created.sessionKey());
        }

        @Test
        void 끝낸_뒤_수집_창_시간이_지나면_받지_않는다() {
            RecordingSessionCreateResponseV2 created = create();
            RecordingSessionV2 session = recordingV2DomainRepository.getSession(created.sessionKey());
            recordingV2DomainRepository.finishSession(session.id(), LocalDateTime.now().minusMinutes(1));

            assertThatThrownBy(() -> recordingServiceV2.upload(created.sessionKey(), created.deviceToken(), "0-1",
                    UPLOAD, audio()))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.RECORDING_SESSION_CLOSED.getMessage());
        }
    }

    @Nested
    class Get {

        @Test
        void 다른_회원의_세션은_조회할_수_없다() {
            RecordingSessionCreateResponseV2 created = create();
            Member other = memberGenerator.generate("other@email.com");

            assertThatThrownBy(() -> recordingServiceV2.get(other, created.sessionKey()))
                    .isInstanceOf(DTClientErrorException.class)
                    .hasMessage(ClientErrorCode.RECORDING_SESSION_NOT_FOUND.getMessage());
        }

        @Test
        void 세션_상태와_턴_목록을_조회한다() {
            RecordingSessionCreateResponseV2 created = create();
            RecordingSessionV2 session = recordingV2DomainRepository.getSession(created.sessionKey());
            recordingV2DomainRepository.createTurn(session.id(), "0-1", 0, null, 0L, 2);

            RecordingSessionResponseV2 response = recordingServiceV2.get(member, created.sessionKey());

            assertAll(
                    () -> assertThat(response.status()).isEqualTo(RecordingSessionStatusV2.RECORDING),
                    () -> assertThat(response.turns()).singleElement()
                            .satisfies(turn -> assertThat(turn.candidateCount()).isEqualTo(2))
            );
        }
    }

    private RecordingSessionCreateResponseV2 create() {
        return recordingServiceV2.create(member, new RecordingSessionCreateRequestV2(table.getId(), "ko-KR"));
    }

    private MockMultipartFile audio() {
        return new MockMultipartFile("audio", "turn.webm", "audio/webm", new byte[40_000]);
    }
}
