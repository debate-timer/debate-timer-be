package com.debatetimer.view.exporter;

import com.debatetimer.domain.parliamentary.ParliamentaryBoxType;
import com.debatetimer.dto.parliamentary.response.ParliamentaryTimeBoxResponse;
import org.springframework.stereotype.Component;

@Component
public class ParliamentaryTableExportMessageResolver {

    private static final String SPEAKER_SUFFIX = "번 토론자";
    private static final String MINUTES_MESSAGE = "분";
    private static final String SECOND_MESSAGE = "초";
    private static final String TIME_MESSAGE_PREFIX = "(";
    private static final String TIME_MESSAGE_SUFFIX = ")";
    private static final String MESSAGE_DELIMITER = "/";
    private static final String SPACE = " ";

    public String resolveBoxMessage(ParliamentaryTimeBoxResponse timeBox) {
        String defaultMessage = resolveDefaultMessage(timeBox);
        ParliamentaryBoxType type = timeBox.type();
        if (type == ParliamentaryBoxType.TIME_OUT) {
            return defaultMessage;
        }
        return defaultMessage
                + MESSAGE_DELIMITER
                + resolveSpeakerMessage(timeBox.speakerNumber());
    }

    private String resolveDefaultMessage(ParliamentaryTimeBoxResponse timeBox) {
        ParliamentaryBoxType boxType = timeBox.type();
        return ParliamentaryBoxTypeView.mapView(boxType)
                + resolveTimeMessage(timeBox.time());
    }

    private String resolveTimeMessage(int totalSecond) {
        StringBuilder messageBuilder = new StringBuilder();
        int minutes = totalSecond / 60;
        int second = totalSecond % 60;

        if (minutes != 0) {
            messageBuilder.append(minutes + MINUTES_MESSAGE + SPACE);
        }
        if (second != 0) {
            messageBuilder.append(second + SECOND_MESSAGE);
        }
        return TIME_MESSAGE_PREFIX
                + messageBuilder
                + TIME_MESSAGE_SUFFIX;
    }

    private String resolveSpeakerMessage(int speakerNumber) {
        return speakerNumber + SPEAKER_SUFFIX;
    }
}
