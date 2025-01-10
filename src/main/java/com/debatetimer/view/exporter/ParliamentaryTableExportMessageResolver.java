package com.debatetimer.view.exporter;

import com.debatetimer.domain.BoxType;
import com.debatetimer.domain.parliamentary.ParliamentaryTimeBox;
import org.springframework.stereotype.Component;

@Component
public class ParliamentaryTableExportMessageResolver {

    private static final String SPEAKER_SUFFIX = "번 토론자";
    private static final String MINUTES_MESSAGE = "분";
    private static final String SECOND_MESSAGE = "초";
    private static final String LEFT_PARENTHESIS = "(";
    private static final String RIGHT_PARENTHESIS = ")";
    private static final String SLASH = "/";
    private static final String SPACE = " ";

    public String resolveBoxMessage(ParliamentaryTimeBox timeBox) {
        String defaultMessage = resolveDefaultMessage(timeBox);
        if (timeBox.getType() == BoxType.TIME_OUT) {
            return defaultMessage;
        }
        return defaultMessage
                + SLASH
                + resolveSpeakerMessage(timeBox.getSpeaker());
    }

    private String resolveDefaultMessage(ParliamentaryTimeBox timeBox) {
        return BoxTypeView.mapView(timeBox.getType())
                + LEFT_PARENTHESIS
                + resolveTimeMessage(timeBox.getTime())
                + RIGHT_PARENTHESIS;
    }

    private String resolveTimeMessage(int totalSecond) {
        int minutes = totalSecond / 60;
        int second = totalSecond % 60;
        String message = minutes + MINUTES_MESSAGE;

        if (second != 0) {
            message += SPACE + second + SECOND_MESSAGE;
        }
        return message;
    }

    private String resolveSpeakerMessage(int speakerNumber) {
        return speakerNumber + SPEAKER_SUFFIX;
    }
}
