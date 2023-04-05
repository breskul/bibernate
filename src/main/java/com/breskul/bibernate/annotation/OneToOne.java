package com.breskul.bibernate.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a single-valued association to another entity that has one-to-one multiplicity.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OneToOne {

    /**
     * (Optional) Whether the association is optional. If set
     * to false then a non-null relationship must always exist.
     */
    boolean optional() default true;
}
