package com.breskul.bibernate.persistence.util.testModel;

import com.breskul.bibernate.annotation.GeneratedValue;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Strategy;

public class EntitySequence {
    @Id
    @GeneratedValue(strategy = Strategy.SEQUENCE)
    private Long id;
    private String name;
    private Integer age;
}