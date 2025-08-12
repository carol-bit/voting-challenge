package com.dbserver.votingchallenge.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VoteChoice {
    SIM("Sim"),
    NAO("Não");

    private final String displayName;

    VoteChoice(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    @Override
    public String toString() {
        return displayName;
    }

    @JsonCreator
    public static VoteChoice fromValue(String value) {
        for (VoteChoice choice : VoteChoice.values()) {
            if (choice.displayName.equalsIgnoreCase(value)) {
                return choice;
            }
        }
        throw new IllegalArgumentException("Invalid value for VoteChoice: " + value);
    }
}
