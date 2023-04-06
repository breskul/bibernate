package com.breskul.bibernate.validate.test_model;

import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;

@Entity
public class TestFetchEntityWithoutDefaultConstructor {

    public TestFetchEntityWithoutDefaultConstructor(Long id) {
        this.id = id;
    }

    @Id
    private Long id;
}
