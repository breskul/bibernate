package com.breskul.bibernate.persistence.util.test_model;

import com.breskul.bibernate.annotation.Id;

public class EntityAuto {
    @Id
    private Long id;
    private String name;
    private Integer age;
}
