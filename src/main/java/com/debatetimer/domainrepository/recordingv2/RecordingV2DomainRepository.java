package com.debatetimer.domainrepository.recordingv2;

import com.debatetimer.domain.customize.Stance;
import com.debatetimer.domain.recordingv2.RecordingSessionStatusV2;
import com.debatetimer.domain.recordingv2.RecordingSessionV2;
import com.debatetimer.domain.recordingv2.RecordingTurnStatusV2;
import com.debatetimer.domain.recordingv2.RecordingTurnV2;
import com.debatetimer.domain.transcript.Utterance;
import com.debatetimer.entity.recordingv2.RecordingSessionV2Entity;
import com.debatetimer.entity.recordingv2.RecordingTurnV2Entity;
import com.debatetimer.repository.recordingv2.RecordingSessionV2Repository;
import com.debatetimer.exception.custom.DTServerErrorException;
import com.debatetimer.exception.errorcode.ServerErrorCode;
import com.debatetimer.repository.recordingv2.RecordingTurnV2Repository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RecordingV2DomainRepository {

    private static final TypeReference<List<Utterance>> UTTERANCES_TYPE = new TypeReference<>() {
    };

    private final RecordingSessionV2Repository sessionRepository;
    private final RecordingTurnV2Repository turnRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public RecordingSessionV2 createSession(String sessionKey, long memberId, long tableId,
                                            @Nullable String language, String joinCodeHash) {
        RecordingSessionV2Entity entity = new RecordingSessionV2Entity(sessionKey, memberId, tableId, language,
                joinCodeHash);
        return toDomain(sessionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public RecordingSessionV2 getSession(String sessionKey) {
        return toDomain(sessionRepository.getBySessionKey(sessionKey));
    }

    @Transactional(readOnly = true)
    public RecordingSessionV2 getSession(long id) {
        return toDomain(sessionRepository.getById(id));
    }

    @Transactional(readOnly = true)
    public List<RecordingSessionV2> findSessions(RecordingSessionStatusV2 status) {
        return sessionRepository.findAllByStatus(status).stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RecordingSessionV2> findSessionsCreatedBefore(RecordingSessionStatusV2 status,
                                                              LocalDateTime createdAt) {
        return sessionRepository.findAllByStatusAndCreatedAtBefore(status, createdAt).stream()
                .map(this::toDomain)
                .toList();
    }

    @Transactional
    public void finishSession(long id, LocalDateTime finishedAt) {
        sessionRepository.getById(id).finish(finishedAt);
    }

    @Transactional
    public void completeSession(long id, long transcriptId) {
        sessionRepository.getById(id).complete(transcriptId);
    }

    @Transactional
    public void failSession(long id, String errorMessage) {
        sessionRepository.getById(id).fail(errorMessage);
    }

    @Transactional
    public RecordingTurnV2 createTurn(long sessionId, String turnKey, int timeBoxIndex, @Nullable Stance team,
                                      long startedAtEpochMs, int candidateCount) {
        RecordingTurnV2Entity entity = new RecordingTurnV2Entity(sessionId, turnKey, timeBoxIndex, team,
                startedAtEpochMs, candidateCount);
        return toDomain(turnRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public boolean existsTurn(long sessionId, String turnKey) {
        return turnRepository.existsBySessionIdAndTurnKey(sessionId, turnKey);
    }

    @Transactional
    public void completeTurn(long turnId, String selectedDeviceId, long startedAtEpochMs, @Nullable Stance team,
                             @Nullable String s3Key, List<Utterance> utterances) {
        turnRepository.getById(turnId)
                .complete(selectedDeviceId, startedAtEpochMs, team, s3Key, writeUtterances(utterances));
    }

    @Transactional
    public void failTurn(long turnId, String errorMessage) {
        turnRepository.getById(turnId).fail(errorMessage);
    }

    /**
     * 처리 중으로 기록된 지 오래된 턴을 실패로 바꾼다.
     *
     * @return 실패로 바꾼 턴 수
     */
    @Transactional
    public int failProcessingTurnsCreatedBefore(LocalDateTime createdAt, String errorMessage) {
        List<RecordingTurnV2Entity> turns = turnRepository.findAllByStatusAndCreatedAtBefore(
                RecordingTurnStatusV2.PROCESSING, createdAt);
        turns.forEach(turn -> turn.fail(errorMessage));
        return turns.size();
    }

    @Transactional(readOnly = true)
    public List<RecordingTurnV2> findTurns(long sessionId) {
        return turnRepository.findAllBySessionIdOrderByIdAsc(sessionId).stream()
                .map(this::toDomain)
                .toList();
    }

    private RecordingSessionV2 toDomain(RecordingSessionV2Entity entity) {
        return new RecordingSessionV2(entity.getId(), entity.getSessionKey(), entity.getMemberId(),
                entity.getTableId(), entity.getLanguage(), entity.getJoinCodeHash(), entity.getStatus(),
                entity.getTranscriptId(), entity.getErrorMessage(), entity.getFinishedAt(), entity.getCreatedAt());
    }

    private RecordingTurnV2 toDomain(RecordingTurnV2Entity entity) {
        return new RecordingTurnV2(entity.getId(), entity.getSessionId(), entity.getTurnKey(),
                entity.getTimeBoxIndex(), entity.getTeam(), entity.getStartedAtEpochMs(), entity.getStatus(),
                entity.getCandidateCount(), entity.getSelectedDeviceId(), entity.getS3Key(),
                readUtterances(entity.getUtterancesJson()), entity.getErrorMessage());
    }

    private String writeUtterances(List<Utterance> utterances) {
        try {
            return objectMapper.writeValueAsString(utterances);
        } catch (JsonProcessingException exception) {
            throw new DTServerErrorException(ServerErrorCode.RECORDING_UTTERANCES_CONVERSION_ERROR, exception);
        }
    }

    private List<Utterance> readUtterances(@Nullable String utterancesJson) {
        if (utterancesJson == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(utterancesJson, UTTERANCES_TYPE);
        } catch (JsonProcessingException exception) {
            throw new DTServerErrorException(ServerErrorCode.RECORDING_UTTERANCES_CONVERSION_ERROR, exception);
        }
    }
}
