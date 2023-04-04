package com.breskul.bibernate.persistence.util.testModel;

import com.breskul.bibernate.annotation.Id;

public class EntityAuto {
    @Id
    private Long id;
    private String name;
    private Integer age;
}
