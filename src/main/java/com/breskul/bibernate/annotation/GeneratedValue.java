package com.breskul.bibernate.annotation;

import com.breskul.bibernate.annotation.enums.Strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratedValue {
	Strategy strategy() default Strategy.SEQUENCE;
}