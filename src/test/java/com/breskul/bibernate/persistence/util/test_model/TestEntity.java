package com.breskul.bibernate.persistence.util.test_model;

import com.breskul.bibernate.annotation.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestEntity {
    @Id
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
