package com.breskul.bibernate.persistence.util.test_model;

import com.breskul.bibernate.annotation.Column;
import com.breskul.bibernate.annotation.Entity;
import com.breskul.bibernate.annotation.Id;
import com.breskul.bibernate.annotation.Table;

@Entity
@Table(name = "test")
public class UpdateQueryTest {
    @Id
    private Long id = 1L;
    @Column(name = "first_name")
    private String firstName = "firstName";
    @Column(name = "last_name")
    private String lastName = "lastName";
}
