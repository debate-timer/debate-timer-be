package com.debatetimer.domain;

import lombok.Getter;

@Getter
public enum Type {

    OPENING("입론"),
    REBUTTAL("반론"),
    CROSS("교차질의"),
    CLOSING("최종발언"),
    TIME_OUT("작전시간"),
    ;

    private final String name;

    Type(String name) {
        this.name = name;
    }
}
