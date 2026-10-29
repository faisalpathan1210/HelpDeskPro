package com.helpdeskpro.helpdeskpro.model;

public enum Status {
    OPEN, IN_PROGRESS, RESOLVED;

    public boolean canMoveTo(Status next) {
        return switch (this) {
            case OPEN -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == RESOLVED;
            case RESOLVED -> false;
        };
    }
}