package com.debatetimer.controller;

public enum Tag {

    MEMBER_API("Member API"),
    PARLIAMENTARY_API("Parliamentary Table API"),
    ;

    private final String displayName;

    Tag(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
