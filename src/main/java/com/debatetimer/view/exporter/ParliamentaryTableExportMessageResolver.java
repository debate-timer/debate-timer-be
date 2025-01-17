package com.debatetimer.view.exporter;

import com.debatetimer.domain.BoxType;
import com.debatetimer.dto.parliamentary.response.TimeBoxResponse;
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

    public String resolveBoxMessage(TimeBoxResponse timeBox) {
        String defaultMessage = resolveDefaultMessage(timeBox);
        BoxType type = BoxType.valueOf(timeBox.type());
        if (type == BoxType.TIME_OUT) {
            return defaultMessage;
        }
        return defaultMessage
                + MESSAGE_DELIMITER
                + resolveSpeakerMessage(timeBox.speakerNumber());
    }

    private String resolveDefaultMessage(TimeBoxResponse timeBox) {
        BoxType boxType = BoxType.valueOf(timeBox.type());
        return BoxTypeView.mapView(boxType)
                + resolveTimeMessage(timeBox.time());
    }

    private String resolveTimeMessage(int totalSecond) {
        int minutes = totalSecond / 60;
        int second = totalSecond % 60;
        String message = minutes + MINUTES_MESSAGE;

        if (second != 0) {
            message += SPACE + second + SECOND_MESSAGE;
        }
        return TIME_MESSAGE_PREFIX
                + message
                + TIME_MESSAGE_SUFFIX;
    }

    private String resolveSpeakerMessage(int speakerNumber) {
        return speakerNumber + SPEAKER_SUFFIX;
    }
}
