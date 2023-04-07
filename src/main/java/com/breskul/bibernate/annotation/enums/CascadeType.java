package com.breskul.bibernate.annotation.enums;
/**
 * The CascadeType enum defines the different types of cascade operations that can be performed on a JPA entity when a related entity is persisted, merged, or removed.
 */
public enum CascadeType {
    /**
     * <p> Indicates that all cascade operations should be performed on the related entity.</p>
     */
    ALL,
    /**
     * <p>Indicates that the related entity should be persisted along with the owning entity.</p>
     */
    PERSIST,
    /**
     * <p>Indicates that changes made to the related entity should be merged into the owning entity.</p>
     */
    MERGE,
    /**
     * <p>Indicates that the related entity should be removed along with the owning entity.</p>
     */
    REMOVE,
    /**
     * <p>Indicates that the related entity not affected.</p>
     */
    NONE
}
