package com.debatetimer.domain.ParliamentaryDebate;

import lombok.Getter;

@Getter
public enum ParliamentaryType {

    OPENING("입론"),
    REBUTTAL("반론"),
    CROSS("교차질의"),
    CLOSING("최종발언"),
    TIME_OUT("작전시간"),
    ;

    private final String name;

    ParliamentaryType(String name) {
        this.name = name;
    }
}
