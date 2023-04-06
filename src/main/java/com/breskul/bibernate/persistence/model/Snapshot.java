package com.breskul.bibernate.persistence.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Snapshot class save entity values after fetch or save entity inside session
 * For new entities status ACTUAL, for removed will set up status REMOVED.
 */
@Getter
@Setter
public class Snapshot {

    String value;
    Status status;

    public Snapshot(String value, Status status) {
        this.value = value;
        this.status = status;
    }

    public enum Status {
        ACTUAL,
        REMOVED,
    }
}
