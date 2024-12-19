package com.debatetimer.domain;

import lombok.Getter;

@Getter
public enum Stance {

    PROS("찬성"),
    CONS("반대"),
    NEUTRAL("중립"),
    ;

    private final String name;

    Stance(String name) {
        this.name = name;
    }
}
